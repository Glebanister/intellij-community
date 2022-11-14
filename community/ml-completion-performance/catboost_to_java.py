import os
import struct
from typing import List, Callable, Any, BinaryIO

import importlib.machinery
from zipfile import ZipFile
from catboost import CatBoostRegressor

import pandas


class CatboostToJavaConverter:
    __FEATURE_FOLDER_PATTERN = '{}_features'
    __MODEL_FILENAME = 'model.bin'
    __LOCAL_MODEL_FILENAME = 'local_model.zip'
    __LANGUAGE_FILENAME = 'languages.txt'

    @staticmethod
    def build_input(path_to_model: str, data: pandas.DataFrame) -> str:
        return path_to_model + '\n' + '\n'.join(' '.join(str(x) for x in row.values) for _, row in data.iterrows())

    @staticmethod
    def build_class_path(output_directory: str) -> str:
        return os.path.join(output_directory, 'test_classes')

    @staticmethod
    def create_py_model_instance(cb_model: CatBoostRegressor, output_dir):
        py_model_dir = os.path.join(output_dir, 'python')
        os.makedirs(py_model_dir, exist_ok=True)
        # generate python model
        python_model_file = os.path.join(py_model_dir, 'model.py')
        cb_model.save_model(python_model_file, format='python')

        # generate fake python module
        fake_module_file = os.path.join(py_model_dir, '__init__.py')
        with open(fake_module_file, 'wb'):
            pass

        # load this module on the fly from output dir
        loader = importlib.machinery.SourceFileLoader('python.model', python_model_file)
        module = loader.load_module('python.model')

        generated_model_class = getattr(module, 'catboost_model')

        # create generated model instance
        return generated_model_class()

    def convert_to_bin_model(self, py_model, output_dir):
        float_features_index: List[int] = py_model.float_features_index
        float_feature_count: int = py_model.float_feature_count
        binary_feature_count: int = py_model.binary_feature_count
        tree_count: int = py_model.tree_count
        float_feature_borders: List[List[float]] = py_model.float_feature_borders
        tree_depth: List[int] = py_model.tree_depth
        tree_split_border: List[int] = py_model.tree_split_border
        tree_split_feature_index: List[int] = py_model.tree_split_feature_index
        tree_split_xor_mask: List[int] = py_model.tree_split_xor_mask
        leaf_values: List[float] = py_model.leaf_values
        scale: float = py_model.scale if hasattr(py_model, 'scale') else 1.0
        bias: float = py_model.bias if hasattr(py_model, 'bias') else 0.0

        model_path = os.path.join(output_dir, CatboostToJavaConverter.__MODEL_FILENAME)
        with open(model_path, "wb") as f:
            self.write_int_list(f, float_features_index)
            self.write_int_number(f, float_feature_count)
            self.write_int_number(f, binary_feature_count)
            self.write_int_number(f, tree_count)
            self.write_nd_list(f, float_feature_borders,
                               lambda f_, e: self.write_double_list(f_, e))
            self.write_int_list(f, tree_depth)
            self.write_int_list(f, tree_split_border)
            self.write_int_list(f, tree_split_feature_index)
            self.write_int_list(f, tree_split_xor_mask)
            self.write_double_list(f, leaf_values)
            self.write_double_number(f, scale)
            self.write_double_number(f, bias)

    @staticmethod
    def write_int_list(f: BinaryIO, data: List[any]):
        CatboostToJavaConverter.__write_list(f, 'i', data)

    @staticmethod
    def write_double_list(f: BinaryIO, data: List[any]):
        CatboostToJavaConverter.__write_list(f, 'd', data)

    @staticmethod
    def __write_list(f: BinaryIO, fchar: str, data: List[any]):
        size = len(data)
        f.write(struct.pack(">i", size))
        byte_buffer = struct.pack(f'>{size}{fchar}', *data)
        f.write(byte_buffer)

    @staticmethod
    def write_int_number(f: BinaryIO, value):
        CatboostToJavaConverter.__write_number(f, 'i', value)

    @staticmethod
    def write_double_number(f: BinaryIO, value):
        CatboostToJavaConverter.__write_number(f, 'd', value)

    @staticmethod
    def __write_number(f: BinaryIO, fchar: str, value):
        f.write(struct.pack(f'>{fchar}', value))

    @staticmethod
    def write_nd_list(f: BinaryIO, data: List[any], write_element: Callable[[BinaryIO, Any], None]):
        size = len(data)
        f.write(struct.pack(">i", size))
        for e in data:
            write_element(f, e)

    @staticmethod
    def zip_local_model(output_dir, language):
        with open(os.path.join(output_dir, CatboostToJavaConverter.__LANGUAGE_FILENAME), 'w') as file:
            file.write(language)

        model_path = os.path.join(output_dir, CatboostToJavaConverter.__MODEL_FILENAME)
        features_folder_name = CatboostToJavaConverter.__FEATURE_FOLDER_PATTERN.format(language)
        features_path = os.path.join(output_dir, features_folder_name)
        print(features_path)

        with ZipFile(os.path.join(output_dir, CatboostToJavaConverter.__LOCAL_MODEL_FILENAME), 'w') as zip_archive:
            zip_archive.write(model_path, CatboostToJavaConverter.__MODEL_FILENAME)
            for folder_name, _, filenames in os.walk(features_path):
                for file in filenames:
                    path = os.path.join(folder_name, file)
                    zip_archive.write(path, os.path.join(features_folder_name, file))
            zip_archive.write(os.path.join(output_dir, CatboostToJavaConverter.__LANGUAGE_FILENAME), CatboostToJavaConverter.__LANGUAGE_FILENAME)
