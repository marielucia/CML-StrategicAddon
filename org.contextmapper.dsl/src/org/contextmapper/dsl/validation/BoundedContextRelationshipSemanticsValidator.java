/*
 * Copyright 2018 The Context Mapper Project Team
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

import static org.contextmapper.dsl.validation.ValidationMessages.CUSTOMER_SUPPLIER_WITH_ACL_WARNING_MESSAGE;
import static org.contextmapper.dsl.validation.ValidationMessages.CUSTOMER_SUPPLIER_WITH_CONFORMIST_ERROR_MESSAGE;
import static org.contextmapper.dsl.validation.ValidationMessages.CUSTOMER_SUPPLIER_WITH_OHS_ERROR_MESSAGE;
import static org.contextmapper.dsl.validation.ValidationMessages.SELF_RELATIONSHIP_NOT_ALLOWED;

import org.contextmapper.dsl.contextMappingDSL.BoundedContext;
import org.contextmapper.dsl.contextMappingDSL.ContextMap;
import org.contextmapper.dsl.contextMappingDSL.ContextMappingDSLPackage;
import org.contextmapper.dsl.contextMappingDSL.CustomerSupplierRelationship;
import org.contextmapper.dsl.contextMappingDSL.DownstreamRole;
import org.contextmapper.dsl.contextMappingDSL.Relationship;
import org.contextmapper.dsl.contextMappingDSL.SymmetricRelationship;
import org.contextmapper.dsl.contextMappingDSL.UpstreamDownstreamRelationship;
import org.contextmapper.dsl.contextMappingDSL.UpstreamRole;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.EValidatorRegistrar;

public class BoundedContextRelationshipSemanticsValidator extends AbstractDeclarativeValidator {

	@Override
	public void register(EValidatorRegistrar registrar) {
		// not needed for classes used as ComposedCheck
	}

	@Check
	public void customerSupplierRolesValidator(final CustomerSupplierRelationship relationship) {
		// Upstream in Customer-Supplier relationship should not implement OHS
		if (relationship.getUpstreamRoles().contains(UpstreamRole.OPEN_HOST_SERVICE))
			error(CUSTOMER_SUPPLIER_WITH_OHS_ERROR_MESSAGE, relationship, ContextMappingDSLPackage.Literals.UPSTREAM_DOWNSTREAM_RELATIONSHIP__UPSTREAM_ROLES);

		// Downstream in Customer-Supplier relationship should not implement ACL
		if (relationship.getDownstreamRoles().contains(DownstreamRole.ANTICORRUPTION_LAYER))
			warning(CUSTOMER_SUPPLIER_WITH_ACL_WARNING_MESSAGE, relationship, ContextMappingDSLPackage.Literals.UPSTREAM_DOWNSTREAM_RELATIONSHIP__DOWNSTREAM_ROLES);

		// Downstream in Customer-Supplier relationship should not implement CONFORMIST
		if (relationship.getDownstreamRoles().contains(DownstreamRole.CONFORMIST))
			error(CUSTOMER_SUPPLIER_WITH_CONFORMIST_ERROR_MESSAGE, relationship, ContextMappingDSLPackage.Literals.UPSTREAM_DOWNSTREAM_RELATIONSHIP__DOWNSTREAM_ROLES);
	}

	@Check
	public void prohibitSelfRelationship(final ContextMap contextMap) {
		int relationshipIndex = 0;
		for (Relationship relationship : contextMap.getRelationships()) {
			BoundedContext context1;
			BoundedContext context2;
			if (relationship instanceof SymmetricRelationship) {
				context1 = ((SymmetricRelationship) relationship).getParticipant1();
				context2 = ((SymmetricRelationship) relationship).getParticipant2();
			} else {
				context1 = ((UpstreamDownstreamRelationship) relationship).getUpstream();
				context2 = ((UpstreamDownstreamRelationship) relationship).getDownstream();
			}
			if (context1 == context2) {
				error(String.format(SELF_RELATIONSHIP_NOT_ALLOWED), contextMap, ContextMappingDSLPackage.Literals.CONTEXT_MAP__RELATIONSHIPS, relationshipIndex);
			}
			relationshipIndex++;
		}
	}

	/* New constraint - no other reationship between two BCs when Separate Ways ist established */
	@Check
	public void prohibitAdditionalRelationshipsWhenSeparateWays(final ContextMap contextMap) {
	    final java.util.List<Relationship> rels = contextMap.getRelationships();

	    for (int i = 0; i < rels.size(); i++) {
	        final Relationship r = rels.get(i);
	        if (!(r instanceof org.contextmapper.dsl.contextMappingDSL.SeparateWays)) continue;

	        final org.contextmapper.dsl.contextMappingDSL.SymmetricRelationship sw =
	            (org.contextmapper.dsl.contextMappingDSL.SymmetricRelationship) r;
	        final BoundedContext a = sw.getParticipant1();
	        final BoundedContext b = sw.getParticipant2();

	        boolean conflictFound = false;
	        for (int j = 0; j < rels.size(); j++) {
	            if (i == j) continue;
	            final Relationship other = rels.get(j);

	            if (other instanceof org.contextmapper.dsl.contextMappingDSL.SymmetricRelationship) {
	                final var s = (org.contextmapper.dsl.contextMappingDSL.SymmetricRelationship) other;
	                final BoundedContext p1 = s.getParticipant1();
	                final BoundedContext p2 = s.getParticipant2();
	                if ((p1 == a && p2 == b) || (p1 == b && p2 == a)) { conflictFound = true; break; }
	            } else if (other instanceof UpstreamDownstreamRelationship) {
	                final var ud = (UpstreamDownstreamRelationship) other;
	                final BoundedContext up = ud.getUpstream();
	                final BoundedContext down = ud.getDownstream();
	                if ((up == a && down == b) || (up == b && down == a)) { conflictFound = true; break; }
	            }
	        }

	        if (conflictFound) {
	             error(String.format(ValidationMessages.SEPARATE_WAYS_EXCLUSIVE_FMT, a.getName(), b.getName()),
	             contextMap, ContextMappingDSLPackage.Literals.CONTEXT_MAP__RELATIONSHIPS, i);
	        }
	    }
	}
}
