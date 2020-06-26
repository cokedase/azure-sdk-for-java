// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Provides parameter values to a magnitude scoring function.
 */
@Fluent
public final class MagnitudeScoringParameters {
    /*
     * The field value at which boosting starts.
     */
    @JsonProperty(value = "boostingRangeStart", required = true)
    private double boostingRangeStart;

    /*
     * The field value at which boosting ends.
     */
    @JsonProperty(value = "boostingRangeEnd", required = true)
    private double boostingRangeEnd;

    /*
     * A value indicating whether to apply a constant boost for field values
     * beyond the range end value; default is false.
     */
    @JsonProperty(value = "constantBoostBeyondRange")
    private Boolean shouldBoostBeyondRangeByConstant;

    /**
     * Constructor of {@link MagnitudeScoringParameters}.
     *
     * @param boostingRangeStart The field value at which boosting starts.
     * @param boostingRangeEnd The field value at which boosting ends.
     */
    @JsonCreator
    public MagnitudeScoringParameters(
        @JsonProperty(value = "boostingRangeStart", required = true) double boostingRangeStart,
        @JsonProperty(value = "boostingRangeEnd", required = true) double boostingRangeEnd) {
        this.boostingRangeStart = boostingRangeStart;
        this.boostingRangeEnd = boostingRangeEnd;
    }

    /**
     * Get the boostingRangeStart property: The field value at which boosting
     * starts.
     *
     * @return the boostingRangeStart value.
     */
    public double getBoostingRangeStart() {
        return this.boostingRangeStart;
    }

    /**
     * Get the boostingRangeEnd property: The field value at which boosting
     * ends.
     *
     * @return the boostingRangeEnd value.
     */
    public double getBoostingRangeEnd() {
        return this.boostingRangeEnd;
    }

    /**
     * Get the shouldBoostBeyondRangeByConstant property: A value indicating
     * whether to apply a constant boost for field values beyond the range end
     * value; default is false.
     *
     * @return the shouldBoostBeyondRangeByConstant value.
     */
    public Boolean shouldBoostBeyondRangeByConstant() {
        return this.shouldBoostBeyondRangeByConstant;
    }

    /**
     * Set the shouldBoostBeyondRangeByConstant property: A value indicating
     * whether to apply a constant boost for field values beyond the range end
     * value; default is false.
     *
     * @param shouldBoostBeyondRangeByConstant the
     * shouldBoostBeyondRangeByConstant value to set.
     * @return the MagnitudeScoringParameters object itself.
     */
    public MagnitudeScoringParameters setShouldBoostBeyondRangeByConstant(Boolean shouldBoostBeyondRangeByConstant) {
        this.shouldBoostBeyondRangeByConstant = shouldBoostBeyondRangeByConstant;
        return this;
    }
}
