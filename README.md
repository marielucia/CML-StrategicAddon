# CML-StrategicAddon
An adapted CML-Version which extends the existing grammar with missing strategic design patterns.

## What's CML
The Context Mapper Language (CML) is a textual domain-specific language (DSL) for modeling Domain-Driven Design systems. It captures strategic and tactical concepts and supports model-driven analysis and rapid prototyping via the Context Mapper toolchain. This repository builds on the existing CML-project (https://github.com/ContextMapper/context-mapper-dsl) createy by Stefan Kapferer and introduces additional strategic design constructs

## What’s new in this project
Context-map additions:
- Separate Ways relationship.
- Big Ball of Mud annotation.
  
Distillation pattern additions:
- Highlighted Core
- Segregated Core
- Abstract Core
- Cohesive Mechansims

## New Metamodel
<img width="886" height="1221" alt="grafik" src="https://github.com/user-attachments/assets/818f0b5b-e5b4-4c6b-8ecf-1b743b54c302" />


## Why
Strategic decisions are often tacit. These extensions encode some of them directly in the model to improve transparency, comprehension, and evolution across teams.
The inclusion of the new strategic DDD-patterns is optional (and accordingly implemented in the code).

## Adapted files
Not all of the originial CML files were adapted, only those necessary to include the above listed patterns in the CML-synatx:
- central CML grammer file: org.contextmapper.dsl/src/org/contextmapper/dsl/ContextMappingDSL.xtext
- validation files to include rules and matching vaidation (error) messages for the new patterns:
    1. org.contextmapper.dsl/src/org/contextmapper/dsl/validation/BoundedContextRelationshipSemanticsValidator.java
    2. org.contextmapper.dsl/src/org/contextmapper/dsl/validation/BoundedContextSemanticsValidator.java
    3. org.contextmapper.dsl/src/org/contextmapper/dsl/validation/ContextMapSemanticsValidator.java
    4. org.contextmapper.dsl/src/org/contextmapper/dsl/validation/ValidationMessages.java
 
## Work environment
This Addon was developed and tested with Eclipse DSL Tools 2023-09 (4.29.0) and Java JDK 11

## License
Apache-2.0 (aligned with the originial CML)

