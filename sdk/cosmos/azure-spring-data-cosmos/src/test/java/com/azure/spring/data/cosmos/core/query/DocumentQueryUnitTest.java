// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.query;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_KEY;
import static com.azure.spring.data.cosmos.common.TestConstants.CRITERIA_OBJECT;

public class DocumentQueryUnitTest {

    @Test
    public void testDocumentQueryCreate() {
        final Criteria criteria = Criteria.getInstance(CriteriaType.IS_EQUAL, CRITERIA_KEY,
                Arrays.asList(CRITERIA_OBJECT));

        final DocumentQuery query = new DocumentQuery(criteria);

        Assert.assertEquals(criteria, query.getCriteria());
        Assert.assertEquals(Sort.unsorted(), query.getSort());
        Assert.assertEquals(Pageable.unpaged(), query.getPageable());
    }
}
