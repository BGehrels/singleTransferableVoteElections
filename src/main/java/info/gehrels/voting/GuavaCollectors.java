/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Library. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class GuavaCollectors {
    public static <R> Collector<R, ImmutableSet.Builder<R>, ImmutableSet<R>> toImmutableSet() {
        return new Collector<R, ImmutableSet.Builder<R>, ImmutableSet<R>>() {
            @Override
            public Supplier<ImmutableSet.Builder<R>> supplier() {
                return ImmutableSet::builder;
            }

            @Override
            public BiConsumer<ImmutableSet.Builder<R>, R> accumulator() {
                return ImmutableSet.Builder::add;
            }

            @Override
            public BinaryOperator<ImmutableSet.Builder<R>> combiner() {
                return (a,b) -> ImmutableSet.<R>builder().addAll(a.build()).addAll(b.build());
            }

            @Override
            public Function<ImmutableSet.Builder<R>, ImmutableSet<R>> finisher() {
                return ImmutableSet.Builder::build;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return EnumSet.noneOf(Characteristics.class);
            }
        };
	}
}
