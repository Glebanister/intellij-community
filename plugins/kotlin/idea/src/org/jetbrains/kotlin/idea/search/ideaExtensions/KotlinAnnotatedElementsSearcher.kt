// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.search.ideaExtensions

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiModifierListOwner
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.Processor
import com.intellij.util.QueryExecutor
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.kotlin.asJava.ImpreciseResolveResult.NO_MATCH
import org.jetbrains.kotlin.asJava.ImpreciseResolveResult.UNSURE
import org.jetbrains.kotlin.asJava.LightClassUtil
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.asJava.toPsiParameters
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.search.PsiBasedClassResolver
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex
import org.jetbrains.kotlin.idea.base.projectStructure.scope.KotlinSourceFilterScope
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode

class KotlinAnnotatedElementsSearcher : QueryExecutor<PsiModifierListOwner, AnnotatedElementsSearch.Parameters> {

    override fun execute(p: AnnotatedElementsSearch.Parameters, consumer: Processor<in PsiModifierListOwner>): Boolean {
        return processAnnotatedMembers(p.annotationClass, p.scope) { declaration ->
            when (declaration) {
                is KtClassOrObject -> {
                    val lightClass = declaration.toLightClass()
                    consumer.process(lightClass)
                }
                is KtNamedFunction, is KtConstructor<*> -> {
                    val wrappedMethod = LightClassUtil.getLightClassMethod(declaration as KtFunction)
                    consumer.process(wrappedMethod)
                }
                is KtProperty -> {
                    val backingField = LightClassUtil.getLightClassBackingField(declaration)
                    if (backingField != null) {
                        return@processAnnotatedMembers consumer.process(backingField)
                    }

                    LightClassUtil.getLightClassPropertyMethods(declaration).all { consumer.process(it) }
                }
                is KtPropertyAccessor -> {
                    val method = LightClassUtil.getLightClassAccessorMethod(declaration)
                    return@processAnnotatedMembers consumer.process(method)
                }
                is KtParameter -> {
                    if (!declaration.toPsiParameters().all { consumer.process(it) }) return@processAnnotatedMembers false
                    LightClassUtil.getLightClassBackingField(declaration)?.let {
                        if (!consumer.process(it)) return@processAnnotatedMembers false
                    }
                    LightClassUtil.getLightClassPropertyMethods(declaration).all { consumer.process(it) }
                }
                else -> true
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance("#com.intellij.psi.impl.search.AnnotatedMembersSearcher")

        fun processAnnotatedMembers(
            annClass: PsiClass,
            useScope: SearchScope,
            preFilter: (KtAnnotationEntry) -> Boolean = { true },
            consumer: (KtDeclaration) -> Boolean
        ): Boolean {
            assert(annClass.isAnnotationType) { "Annotation type should be passed to annotated members search" }

            val psiBasedClassResolver = PsiBasedClassResolver.getInstance(annClass)
            val annotationFQN = annClass.qualifiedName!!

            val candidates = getKotlinAnnotationCandidates(annClass, useScope)
            for (elt in candidates) {
                if (notKtAnnotationEntry(elt)) continue

                val result = runReadAction(fun(): Boolean {
                    if (elt !is KtAnnotationEntry) return true
                    if (!preFilter(elt)) return true

                    val declaration = elt.getStrictParentOfType<KtDeclaration>() ?: return true

                    val psiBasedResolveResult = elt.calleeExpression?.constructorReferenceExpression?.let { ref ->
                        psiBasedClassResolver.canBeTargetReference(ref)
                    }

                    if (psiBasedResolveResult == NO_MATCH) return true
                    if (psiBasedResolveResult == UNSURE) {
                        val context = elt.analyze(BodyResolveMode.PARTIAL)
                        val annotationDescriptor = context.get(BindingContext.ANNOTATION, elt) ?: return true

                        val fqName = annotationDescriptor.fqName ?: return true
                        if (fqName.asString() != annotationFQN) return true
                    }

                    if (!consumer(declaration)) return false

                    return true
                })
                if (!result) return false
            }

            return true
        }

        /* Return all elements annotated with given annotation name. Aliases don't work now. */
        private fun getKotlinAnnotationCandidates(annClass: PsiClass, useScope: SearchScope): Collection<PsiElement> {
            return runReadAction(fun(): Collection<PsiElement> {
                if (useScope is GlobalSearchScope) {
                    val name = annClass.name ?: return emptyList()
                    val scope = KotlinSourceFilterScope.everything(useScope, annClass.project)
                    return KotlinAnnotationsIndex.get(name, annClass.project, scope)
                }

                return (useScope as LocalSearchScope).scope.flatMap { it.collectDescendantsOfType<KtAnnotationEntry>() }
            })
        }

        private fun notKtAnnotationEntry(found: PsiElement): Boolean {
            if (found is KtAnnotationEntry) return false

            val faultyContainer = PsiUtilCore.getVirtualFile(found)
            LOG.error("Non annotation in annotations list: $faultyContainer; element:$found")
            if (faultyContainer != null && faultyContainer.isValid) {
                FileBasedIndex.getInstance().requestReindex(faultyContainer)
            }

            return true
        }
    }

}
