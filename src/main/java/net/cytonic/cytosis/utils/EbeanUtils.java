package net.cytonic.cytosis.utils;

import io.ebean.typequery.PInstant;
import io.ebean.typequery.QueryBean;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EbeanUtils {

    public static <T, R extends QueryBean<T, R>> void orderByInstant(PInstant<R> pBase, boolean orderBy) {
        if (orderBy) {
            pBase.asc();
            return;
        }
        pBase.desc();
    }
}
