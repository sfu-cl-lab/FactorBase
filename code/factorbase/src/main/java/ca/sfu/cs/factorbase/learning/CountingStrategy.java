package ca.sfu.cs.factorbase.learning;


/**
 * enum to help toggle features during FactorBase execution.
 */
public enum CountingStrategy {
    PreCount {
        @Override
        public boolean isPrecount() {
            return true;
        }
    },
    OnDemand {
        @Override
        public boolean isOndemand() {
            return true;
        }


        @Override
        public boolean useCTCache() {
            return true;
        }


        @Override
        public String getStorageEngine() {
            return "MEMORY";
        }
    },
    Hybrid {
        @Override
        public boolean isHybrid() {
            return true;
        }


        @Override
        public boolean useCTCache() {
            return true;
        }


        @Override
        public boolean useProjection() {
            return true;
        }


        @Override
        public String getStorageEngine() {
            return "MEMORY";
        }
    };

    /**
     * Determine if the counting strategy is precount.
     *
     * @return true if the counting strategy is precount; otherwise false.
     */
    public boolean isPrecount() {
        return false;
    }


    /**
     * Determine if the counting strategy is ondemand.
     *
     * @return true if the counting strategy is ondemand; otherwise false.
     */
    public boolean isOndemand() {
        return false;
    }


    /**
     * Determine if the counting strategy is hybrid.
     *
     * @return true if the counting strategy is hybrid; otherwise false.
     */
    public boolean isHybrid() {
        return false;
    }


    /**
     * Determine if the _CT_cache database should be used to cache counts and CT tables.
     *
     * @return true if the _CT_cache database should be used; otherwise false.
     */
    public boolean useCTCache() {
        return false;
    }


    /**
     * Determine if the local counts should be generated by projecting the count information from the global
     * contingency tables.
     *
     * @return true if the counts should be generated by projecting from the global counts tables; otherwise false.
     */
    public boolean useProjection() {
        return false;
    }


    /**
     * Retrieve the storage engine that should be used for the "_star", "_flat", "_false", "_counts", and
     * "_CT" tables.
     *
     * @return the storage engine that should be used for the "_star", "_flat", "_false", "_counts", and
     *         "_CT" tables.
     */
    public String getStorageEngine() {
        return "InnoDB";
    }

    private static final String PRECOUNT = "0";
    private static final String ONDEMAND = "1";
    private static final String HYBRID = "2";


    /**
     * Determine what counting strategy should be used based on the given configuration file setting.
     *
     * @param configurationValue - the counting strategy setting given in the configuration file.
     * @return {@code CountingStrategy} that has been set in the configuration file.
     */
    public static CountingStrategy determineStrategy(String configurationValue) {
        CountingStrategy strategy;
        switch (configurationValue) {
        case PRECOUNT:
            strategy = PreCount;
            break;
        case ONDEMAND:
            strategy = OnDemand;
            break;
        case HYBRID:
            strategy = Hybrid;
            break;
        default:
            strategy = null;
        }

        return strategy;
    }
}