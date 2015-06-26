/*
 * Copyright (c) 2002-2015 "Neo Technology,"
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
package org.neo4j.kernel.api.index;

import java.util.Collections;
import java.util.Set;

import org.neo4j.collection.primitive.PrimitiveLongCollections;
import org.neo4j.collection.primitive.PrimitiveLongIterator;
import org.neo4j.graphdb.Resource;
import org.neo4j.kernel.api.exceptions.index.IndexNotFoundKernelException;

import static org.neo4j.register.Register.DoubleLong;

/**
 * Reader for an {@link IndexAccessor}.
 * Must honor repeatable reads, which means that if a lookup is executed multiple times the same result set
 * must be returned.
 */
public interface IndexReader extends Resource
{
    /**
     * Index seek by value
     */
    PrimitiveLongIterator indexSeek( Object value );

    /**
     * Index seek by prefix search
     */
    PrimitiveLongIterator indexSeekByPrefix( String prefix );

    /**
     * Index scan for all objects
     */
    PrimitiveLongIterator scan();

    /**
     * Number of nodes indexed by the given property
     */
    int getIndexedCount( long nodeId, Object propertyValue );

    /**
     *
     * @return the set of value types present in the index
     */
    Set<Class> valueTypesInIndex();

    /**
     * Sample this index (on the current thread)
     * @param result contains the unique values and the sampled size
     * @return the index size
     * @throws IndexNotFoundKernelException if the index is dropped while sampling
     */
    long sampleIndex( DoubleLong.Out result ) throws IndexNotFoundKernelException;

    class Delegator implements IndexReader
    {
        private final IndexReader delegate;

        public Delegator( IndexReader delegate )
        {
            this.delegate = delegate;
        }

        @Override
        public PrimitiveLongIterator indexSeek( Object value )
        {
            return delegate.indexSeek( value );
        }

        @Override
        public PrimitiveLongIterator indexSeekByPrefix( String prefix )
        {
            return delegate.indexSeekByPrefix( prefix );
        }

        @Override
        public PrimitiveLongIterator scan()
        {
            return delegate.scan();
        }

        @Override
        public int getIndexedCount( long nodeId, Object propertyValue )
        {
            return delegate.getIndexedCount( nodeId, propertyValue );
        }

        @Override
        public Set<Class> valueTypesInIndex()
        {
            return delegate.valueTypesInIndex();
        }

        @Override
        public long sampleIndex( DoubleLong.Out result ) throws IndexNotFoundKernelException
        {
            return delegate.sampleIndex( result );
        }

        @Override
        public void close()
        {
            delegate.close();
        }

        @Override
        public String toString()
        {
            return delegate.toString();
        }
    }

    IndexReader EMPTY = new IndexReader()
    {
        @Override
        public PrimitiveLongIterator indexSeek( Object value )
        {
            return PrimitiveLongCollections.emptyIterator();
        }

        @Override
        public PrimitiveLongIterator indexSeekByPrefix( String prefix )
        {
            return PrimitiveLongCollections.emptyIterator();
        }

        @Override
        public PrimitiveLongIterator scan()
        {
            return PrimitiveLongCollections.emptyIterator();
        }

        // Used for checking index correctness
        @Override
        public int getIndexedCount( long nodeId, Object propertyValue )
        {
            return 0;
        }

        @Override
        public Set<Class> valueTypesInIndex()
        {
            return Collections.emptySet();
        }

        @Override
        public long sampleIndex( DoubleLong.Out result )
        {
            result.write( 0l, 0l );
            return 0;
        }

        @Override
        public void close()
        {
        }
    };
}
