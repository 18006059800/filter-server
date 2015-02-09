package com.zyx.filter.util;

/**
 * 时间单位
 * @author Ervin.zhang
 */
public enum TimeUnit {

    MILLISECONDS {
        public long toMillis(long d)  { return d; }
    },

    SECONDS {
        public long toMillis(long d)  { return x(d, C3/C2, MAX/(C3/C2)); }
    },

    MINUTES {
        public long toMillis(long d)  { return x(d, C4/C2, MAX/(C4/C2)); }
    },

    HOURS {
        public long toMillis(long d)  { return x(d, C5/C2, MAX/(C5/C2)); }
    },

    DAYS {
        public long toMillis(long d)  { return x(d, C6/C2, MAX/(C6/C2)); }
    },
    
    // 以下三个是额外增加的
    WEEKS {
    	public long toMillis(long d)  { return x(d, C7/C2, MAX/(C7/C2)); }
    }, 
    // 不支持toMillis
    MONTHS, YEARS;
    
    static final long C0 = 1L;
    static final long C1 = C0 * 1000L;
    static final long C2 = C1 * 1000L;
    static final long C3 = C2 * 1000L;
    static final long C4 = C3 * 60L;
    static final long C5 = C4 * 60L;
    static final long C6 = C5 * 24L;
    static final long C7 = C6 * 7L;
    

    static final long MAX = Long.MAX_VALUE;

    static long x(long d, long m, long over) {
        if (d >  over) return Long.MAX_VALUE;
        if (d < -over) return Long.MIN_VALUE;
        return d * m;
    }

    /**
     * 转换成时间戳
     */
    public long toMillis(long duration) {
        throw new AbstractMethodError();
    }
    
    /**
     * @return {@link java.util.concurrent.TimeUnit}的时间单位
     * 
     */
    public java.util.concurrent.TimeUnit getJavaTimeUnit() {
    	return java.util.concurrent.TimeUnit.valueOf(this.name());
    }

}
