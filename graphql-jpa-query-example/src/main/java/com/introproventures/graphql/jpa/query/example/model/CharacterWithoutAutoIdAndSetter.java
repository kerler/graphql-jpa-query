/*
 * Copyright 2017 IntroPro Ventures Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.introproventures.graphql.jpa.query.example.model;

import java.util.Set;

import javax.persistence.*;

import com.introproventures.graphql.jpa.query.annotation.GraphQLDescription;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@GraphQLDescription("Abstract representation of an entity in the Star Wars Universe")
@ToString
@EqualsAndHashCode(exclude={"appearsIn","friends"}) // Fixes NPE in Hibernate when initializing loaded collections #1
public abstract class CharacterWithoutAutoIdAndSetter {

    @Id
    @GraphQLDescription("Primary Key for the Character Class")
    String id;

    @GraphQLDescription("Name of the character")
    String name;

    @GraphQLDescription("Who are the known friends to this character")
    @ManyToMany
    @JoinTable(name="characterwithoutautoidandsetter_friends",
            joinColumns=@JoinColumn(name="source_id", referencedColumnName="id"),
            inverseJoinColumns=@JoinColumn(name="friend_id", referencedColumnName="id"))
    Set<CharacterWithoutAutoIdAndSetter> friends;

    @GraphQLDescription("What Star Wars episodes does this character appear in")
    @ElementCollection(targetClass = Episode.class)
    @Enumerated(EnumType.ORDINAL)
    @OrderBy
    Set<Episode> appearsIn;

    CharacterWithoutAutoIdAndSetter() {}
}