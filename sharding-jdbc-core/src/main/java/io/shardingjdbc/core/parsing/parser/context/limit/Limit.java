/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.parser.context.limit;

import io.shardingjdbc.core.parsing.parser.exception.SQLParsingException;
import io.shardingjdbc.core.util.NumberUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Limit object.
 *
 * @author zhangliang
 * @author caohao
 */
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public final class Limit {
    
    private final boolean rowCountRewriteFlag;
    
    private LimitValue offset;
    
    private LimitValue rowCount;
    
    /**
     * Get offset value.
     * 
     * @return offset value
     */
    public int getOffsetValue() {
        return null != offset ? offset.getValue() : 0;
    }
    
    /**
     * Get row count value.
     *
     * @return row count value
     */
    public int getRowCountValue() {
        return null != rowCount ? rowCount.getValue() : -1;
    }
    
    /**
     * Fill parameters for rewrite limit.
     *
     * @param parameters parameters
     * @param isFetchAll is fetch all data or not
     */
    public void processParameters(final List<Object> parameters, final boolean isFetchAll) {
        fill(parameters);
        rewrite(parameters, isFetchAll);
    }
    
    private void fill(final List<Object> parameters) {
        int offset = 0;
        if (null != this.offset) {
            offset = -1 == this.offset.getIndex() ? getOffsetValue() : NumberUtil.roundHalfUp(parameters.get(this.offset.getIndex()));
            this.offset.setValue(offset);
        }
        int rowCount = 0;
        if (null != this.rowCount) {
            rowCount = -1 == this.rowCount.getIndex() ? getRowCountValue() : NumberUtil.roundHalfUp(parameters.get(this.rowCount.getIndex()));
            this.rowCount.setValue(rowCount);
        }
        if (offset < 0 || rowCount < 0) {
            throw new SQLParsingException("LIMIT offset and row count can not be a negative value.");
        }
    }
    
    private void rewrite(final List<Object> parameters, final boolean isFetchAll) {
        int rewriteOffset = 0;
        int rewriteRowCount;
        if (isFetchAll) {
            rewriteRowCount = Integer.MAX_VALUE;
        } else if (rowCountRewriteFlag) {
            rewriteRowCount = null == rowCount ? -1 : getOffsetValue() + rowCount.getValue();
        } else {
            rewriteRowCount = rowCount.getValue();
        }
        if (null != offset && offset.getIndex() > -1) {
            parameters.set(offset.getIndex(), rewriteOffset);
        }
        if (null != rowCount && rowCount.getIndex() > -1) {
            parameters.set(rowCount.getIndex(), rewriteRowCount);
        }
    }
}
