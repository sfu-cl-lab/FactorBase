CREATE TABLE CallLogs (
    CallNumber INT,
    cascadeFS INT, -- MetaData
    lattice INT, -- MetaData
    populateMQ INT, -- MetaData
    populateMQRChain INT, -- MetaData
    buildPVarsCounts INT, -- Counts
    buildRNodeCounts INT, -- Add to Counts, Subtract from Mobius Join
    buildRChainCounts INT, -- Counts
    createJoinTableQueries INT, -- Mobius Join
    buildFlatStarCT INT -- Mobius Join
) ENGINE = MEMORY;