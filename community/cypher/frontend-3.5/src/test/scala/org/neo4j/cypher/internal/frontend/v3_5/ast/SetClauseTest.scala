/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.frontend.v3_5.ast

import org.neo4j.cypher.internal.util.v3_5.DummyPosition
import org.neo4j.cypher.internal.frontend.v3_5.semantics.{SemanticFunSuite, SemanticState}
import org.neo4j.cypher.internal.util.v3_5.symbols._
import org.neo4j.cypher.internal.v3_5.expressions.{DummyExpression, Property, PropertyKeyName}

class SetClauseTest extends SemanticFunSuite {

  test("shouldHaveMergedTypesOfAllAlternativesInSimpleCase") {

    val mapLiteral = DummyExpression(CTMap)
    val property = Property(mapLiteral, PropertyKeyName("key")(DummyPosition(3)))(DummyPosition(5))
    val setItem = SetPropertyItem(property, DummyExpression(CTAny))(DummyPosition(42))
    val setClause = SetClause(Seq(setItem))(DummyPosition(6))


    val result = setClause.semanticCheck(SemanticState.clean)

    result.errors should have size 1
    result.errors.head.msg should startWith("Type mismatch: expected Node or Relationship but was Map")
  }
}
