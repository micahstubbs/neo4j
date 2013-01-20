/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.mutation

import org.neo4j.cypher.internal.symbols.SymbolTable
import org.neo4j.cypher.internal.pipes.{QueryState}
import org.neo4j.graphdb.{Relationship, Node, PropertyContainer}
import org.neo4j.cypher.internal.commands.expressions.{Expression, Property}
import org.neo4j.cypher.internal.ExecutionContext

case class PropertySetAction(prop: Property, e: Expression)
  extends UpdateAction with GraphElementPropertyFunctions {
  val Property(mapExpr, propertyKey) = prop

  def exec(context: ExecutionContext, state: QueryState) = {
    val value = makeValueNeoSafe(e(context))
    val entity = mapExpr(context).asInstanceOf[PropertyContainer]

    (value, entity) match {
      case (null, n: Node)         => state.queryContext.nodeOps().removeProperty(n, propertyKey)
      case (null, r: Relationship) => state.queryContext.relationshipOps().removeProperty(r, propertyKey)
      case (_, r: Relationship)    => state.queryContext.relationshipOps().setProperty(r, propertyKey, value)
      case (_, n: Node)            => state.queryContext.nodeOps().setProperty(n, propertyKey, value)
    }

    state.propertySet.increase()

    Stream(context)
  }

  def identifiers = Seq.empty

  def children = Seq(prop, e)

  def rewrite(f: (Expression) => Expression): UpdateAction = PropertySetAction(prop, e.rewrite(f))

  def throwIfSymbolsMissing(symbols: SymbolTable) {
    e.symbolDependenciesMet(symbols)
  }

  def symbolTableDependencies = prop.symbolTableDependencies ++ e.symbolTableDependencies
}