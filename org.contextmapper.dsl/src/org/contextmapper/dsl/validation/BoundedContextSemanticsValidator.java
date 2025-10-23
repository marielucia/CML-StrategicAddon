/*
 * Copyright 2019 The Context Mapper Project Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.contextmapper.dsl.validation;

import static org.contextmapper.dsl.validation.ValidationMessages.ALREADY_IMPLEMENTED_SUBDOMAIN;
import static org.contextmapper.dsl.validation.ValidationMessages.MULTIPLE_DOMAINS_IMPLEMENTED;

import java.util.List;
import java.util.stream.Collectors;

import org.contextmapper.dsl.contextMappingDSL.BoundedContext;
import org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage;
import org.contextmapper.dsl.contextMappingDSL.Domain;
import org.contextmapper.dsl.contextMappingDSL.Subdomain;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

public class BoundedContextSemanticsValidator extends AbstractDeclarativeValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}

	@Check
	public void validateThatAggregateContainsOnlyOneAggregateRoot(final BoundedContext boundedContext) {
		List<Domain> domains = boundedContext.getImplementedDomainParts().stream().filter(domainPart -> domainPart instanceof Domain).map(domainPart -> (Domain) domainPart)
				.collect(Collectors.toList());

		if (domains.isEmpty())
			return;

		boundedContext.getImplementedDomainParts().stream().filter(domainPart -> domainPart instanceof Subdomain).map(domainPart -> (Subdomain) domainPart).forEach(subdomain -> {
			Domain parentDomain = (Domain) subdomain.eContainer();
			if (domains.contains(parentDomain))
				error(String.format(ALREADY_IMPLEMENTED_SUBDOMAIN, subdomain.getName(), parentDomain.getName()), boundedContext,
						ContextMappingDSLPackage.Literals.BOUNDED_CONTEXT__IMPLEMENTED_DOMAIN_PARTS, boundedContext.getImplementedDomainParts().indexOf(subdomain));
		});
	}

	@Check
	public void warnUserIfABoundedContextImplementsMultipleDomains(final BoundedContext boundedContext) {
		List<Domain> domains = boundedContext.getImplementedDomainParts().stream().filter(domainPart -> domainPart instanceof Domain).map(domainPart -> (Domain) domainPart)
				.collect(Collectors.toList());
		if (domains.size() > 1) {
			for (Domain domain : domains) {
				warning(MULTIPLE_DOMAINS_IMPLEMENTED, boundedContext, ContextMappingDSLPackage.Literals.BOUNDED_CONTEXT__IMPLEMENTED_DOMAIN_PARTS,
						boundedContext.getImplementedDomainParts().indexOf(domain));
			}
		}
	}

	/* New constraint - highlighted elments must exist within a BC */
	@Check
	public void checkHighlightedCore_elementsExistInBC(final org.contextmapper.dsl.contextMappingDSL.BoundedContext bc) {
	    // erlaubte Namen im BC sammeln
	    final java.util.Set<String> names = new java.util.HashSet<>();
	    for (org.contextmapper.dsl.contextMappingDSL.SculptorModule m : bc.getModules()) names.add(m.getName());
	    for (org.contextmapper.dsl.contextMappingDSL.Aggregate a : bc.getAggregates()) names.add(a.getName());
	    for (org.contextmapper.tactic.dsl.tacticdsl.Service s : bc.getDomainServices()) names.add(s.getName());
	    if (bc.getApplication() != null)
	        for (org.contextmapper.tactic.dsl.tacticdsl.Service s : bc.getApplication().getServices()) names.add(s.getName());

	    for (org.contextmapper.dsl.contextMappingDSL.HighlightedCore hc : bc.getHighlightedCores()) {
	        if (hc.getHighlightedElements() == null) continue;
	        int idx = 0;
	        for (String id : hc.getHighlightedElements()) {
	            if (!names.contains(id)) {
	                error(org.contextmapper.dsl.validation.ValidationMessages.HIGHLIGHTED_CORE_ELEMENTS_MUST_EXIST,
	                      hc,
	                      org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage.Literals.HIGHLIGHTED_CORE__HIGHLIGHTED_ELEMENTS,
	                      idx);
	            }
	            idx++;
	        }
	    }
	}

	/* New constraint - highlighted elments must be explicitly referenced */
	@Check
	public void checkHighlightedCore_needsElements(final org.contextmapper.dsl.contextMappingDSL.BoundedContext bc) {
	    for (org.contextmapper.dsl.contextMappingDSL.HighlightedCore hc : bc.getHighlightedCores()) {
	        if (hc.getHighlightedElements() == null || hc.getHighlightedElements().isEmpty()) {
	            error(org.contextmapper.dsl.validation.ValidationMessages.HIGHLIGHTED_CORE_NEEDS_ELEMENTS,
	                  hc,
	                  org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage.Literals.HIGHLIGHTED_CORE__HIGHLIGHTED_ELEMENTS);
	        }
	    }
	}

	/* New constraint - 1 supporting OR 1 core elements must be explicitly referenced */
	@org.eclipse.xtext.validation.Check
	public void checkSegregatedCore_atLeastOneSideSet(final org.contextmapper.dsl.contextMappingDSL.BoundedContext bc) {
	    // Durchlaufe alle SegregatedCore-Instanzen im BC
	    for (org.contextmapper.dsl.contextMappingDSL.SegregatedCore sc : bc.getSegregatedCores()) {
	        boolean hasCoreSide = sc.getCoreElements() != null && !sc.getCoreElements().isEmpty();
	        boolean hasSuppSide = sc.getSupportingElements() != null && !sc.getSupportingElements().isEmpty();

	        if (!hasCoreSide && !hasSuppSide) {
	            // Mindestens eine Seite (coreElements oder supportingElements) muss gesetzt sein
	            error(
	                org.contextmapper.dsl.validation.ValidationMessages.SEGREGATED_CORE_AT_LEAST_ONE_SIDE,
	                sc,
	                org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage.Literals.SEGREGATED_CORE__CORE_ELEMENTS
	            );
	        }
	    }
	}

	/* New constraint - supporting elements cannot be core elements (and the other way around) */
	@Check
	public void checkSegregatedCore_noOverlap(final org.contextmapper.dsl.contextMappingDSL.BoundedContext bc) {
	    for (var sc : bc.getSegregatedCores()) {
	        if (sc.getCoreElements() == null || sc.getSupportingElements() == null) continue;
	        java.util.Set<String> core = new java.util.HashSet<>(sc.getCoreElements());
	        for (int i=0; i<sc.getSupportingElements().size(); i++) {
	            String id = sc.getSupportingElements().get(i);
	            if (core.contains(id)) {
	                error(ValidationMessages.SEGREGATED_CORE_NO_OVERLAP,
	                      sc,
	                      org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage
	                          .Literals.SEGREGATED_CORE__SUPPORTING_ELEMENTS,
	                      i);
	            }
	        }
	    }
	}

	/* New constraint - core or supporting elments must exist within a BC */
	@Check
	public void checkSegregatedCore_elementsExistInBC(final org.contextmapper.dsl.contextMappingDSL.BoundedContext bc) {
	    for (var sc : bc.getSegregatedCores()) {
	        java.util.Set<String> names = new java.util.HashSet<>();
	        for (var m : bc.getModules()) names.add(m.getName());
	        for (var a : bc.getAggregates()) names.add(a.getName());
	        for (var s : bc.getDomainServices()) names.add(s.getName());
	        if (bc.getApplication() != null)
	            for (var s : bc.getApplication().getServices()) names.add(s.getName());

	        // coreElements prüfen
	        if (sc.getCoreElements() != null) {
	            for (int i=0; i<sc.getCoreElements().size(); i++) {
	                String id = sc.getCoreElements().get(i);
	                if (!names.contains(id)) {
	                    error(ValidationMessages.SEGREGATED_CORE_ELEMENTS_MUST_EXIST,
	                          sc,
	                          org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage
	                              .Literals.SEGREGATED_CORE__CORE_ELEMENTS,
	                          i);
	                }
	            }
	        }
	        // supportingElements prüfen
	        if (sc.getSupportingElements() != null) {
	            for (int i=0; i<sc.getSupportingElements().size(); i++) {
	                String id = sc.getSupportingElements().get(i);
	                if (!names.contains(id)) {
	                    error(ValidationMessages.SEGREGATED_CORE_ELEMENTS_MUST_EXIST,
	                          sc,
	                          org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage
	                              .Literals.SEGREGATED_CORE__SUPPORTING_ELEMENTS,
	                          i);
	                }
	            }
	        }
	    }
	}

	/* New constraint - if scope is set as MULTI_BC, multiple BC must be referenced */
	@org.eclipse.xtext.validation.Check
	public void checkCohesiveMechanisms_scopeMultiBCExpectation(final org.contextmapper.dsl.contextMappingDSL.BoundedContext bc) {
	    for (org.contextmapper.dsl.contextMappingDSL.CohesiveMechanism cm : bc.getMechanisms()) {
	        if (cm.getScope() == org.contextmapper.dsl.contextMappingDSL.CMScope.MULTI_BC) {
	            warning(
	                org.contextmapper.dsl.validation.ValidationMessages.COHESIVE_MECH_SCOPE_MULTI_BC_EXPECTS_MULTI_BC,
	                cm,
	                org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage.Literals.COHESIVE_MECHANISM__SCOPE
	            );
	        }
	    }
	}
}
