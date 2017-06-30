

use unielwin_BN;




drop table if exists TargetParents;

create table TargetParents as 
select child as TargetNode, parent as TargetParent
from Path_BayesNets
where rchain='`a,b`' 
and ( child in ( select 1nid from 1Nodes ) or
	  child in ( select 2nid from 2Nodes ) or
	  child in ( select rnid from RNodes ) )
and parent <> '';



drop table if exists TargetChildren;

create table TargetChildren as
select parent as TargetNode, child as TargetChild
from Path_BayesNets
where rchain='`a,b`'
and ( parent in ( select 1nid from 1Nodes ) or
	  parent in ( select 2nid from 2Nodes ) or
	  parent in ( select rnid from RNodes ) )
and child <> '';



drop table if exists TargetChildrensParents;

create table TargetChildrensParents as
select Path_BayesNets.parent as TargetNode,
	   TargetParents.TargetParent as TargetChildParent
from TargetParents, Path_BayesNets
where TargetParents.TargetNode in 
	( select TargetChildren.TargetChild 
	  from TargetChildren
	  where TargetChildren.TargetNode = Path_BayesNets.Parent )
and TargetParents.TargetParent <> Path_BayesNets.Parent
group by Path_BayesNets.parent, TargetParents.TargetParent;




drop table if exists TargetMB;

create table TargetMB as
	( select TargetNode, TargetParent as TargetMBNode
	  from TargetParents )
	union distinct
	( select TargetNode, TargetChild as TargetMBNode
	  from TargetChildren ) 
	union distinct
	( select TargetNode, TargetChildParent as TargetMBNode
	  from TargetChildrensParents )
	ORDER BY TargetNode;
	
