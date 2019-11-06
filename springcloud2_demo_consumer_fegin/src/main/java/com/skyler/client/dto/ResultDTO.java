package com.skyler.client.dto;

import lombok.Data;

/**
 * Description:
 * <pre>
 *
 * </pre>
 * NB.
 *
 * @author skyler
 * Created by on 2019-11-06 at 22:54
 */
@Data
public class ResultDTO<V> {
    private V data;
    protected Integer code;
    protected String message;
}

