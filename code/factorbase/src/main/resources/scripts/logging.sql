CREATE TABLE CallLogs (
    CallNumber INT,
    cascadeFS INT, -- MetaData
    lattice INT, -- MetaData
    populateMQ INT, -- MetaData
    populateMQRChain INT, -- MetaData
    buildPVarsCounts INT, -- Counts
    buildRChainCounts INT, -- Counts
    buildFlatStarCT INT -- Mobius Join
);