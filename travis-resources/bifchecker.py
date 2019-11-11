"""
Script to help analyze BIF files.

"""
from argparse import ArgumentParser
from collections import defaultdict
from xml.etree import ElementTree

GOOD = 0
ERROR = 1


def extractNamespaceMapping(rootElement, key):
    """
    Generate a mapping between the provided key and the namespace in the given BIF element.

    Args:
        rootElement (Element): The Element for the BIF tag.
        key (str): The key to associate with the namespace in the given BIF element.

    Returns:
       dict: Dictionary containing the key:value pair key:namespace.

    """
    nameSpace, _rightBrace, _bifTagName = rootElement.tag[1:].partition('}')
    return {
        key: nameSpace
    }


def extractGraphname(file):
    """
    Retrieve the name of the graph in the given BIF file.

    Args:
        file (str): Path to the BIF file to retrieve the name of the graph from.

    Returns:
        str: The name of the graph in the given BIF file.

    """
    # Extract the namespace information from the BIF file.
    bifElement = ElementTree.parse(file).getroot()
    mapping = extractNamespaceMapping(bifElement, 'bifns')

    # Extract the 'NETWORK' tag element.
    networkElement = bifElement.find('bifns:NETWORK', mapping)

    return networkElement.find('bifns:NAME', mapping).text


def extractStructureInformation(file):
    """
    Extract the node and edge information in the given BIF file.

    Args:
        file (str): Path to the BIF file to extract information from.

    Returns:
        dict: Dictionary containing key:value pairs nodeID:nodeValues.
        dict: Dictionary containing key:value pairs nodeID:parentNodeIDs.

    """
    # Extract the namespace information from the BIF file.
    bifElement = ElementTree.parse(file).getroot()
    mapping = extractNamespaceMapping(bifElement, 'bifns')

    nodeValues = defaultdict(list)
    nodeParents = defaultdict(list)

    # Extract the 'NETWORK' tag element.
    networkElement = bifElement.find('bifns:NETWORK', mapping)

    # Extract the 'VARIABLE' tag elements (Graph Node + Possible Values).
    for variableElement in networkElement.findall('bifns:VARIABLE', mapping):
        # Extract the node name.
        nodeName = variableElement.find('bifns:NAME', mapping).text

        # Extract the values for the node.
        for outcomeElement in variableElement.findall('bifns:OUTCOME', mapping):
            nodeValues[nodeName].append(outcomeElement.text)

    # Extract the 'DEFINITION' tag elements (Graph Node + Parents).
    for definitionElement in networkElement.findall('bifns:DEFINITION', mapping):
        # Extract the child name.
        childName = definitionElement.find('bifns:FOR', mapping).text

        # Extract the parents for the node.
        givenElements = definitionElement.findall('bifns:GIVEN', mapping)
        if not givenElements:
            # If there are no parents, add an empty list.
            nodeParents[childName] = []
        else:
            # Add all the parents for the given child.
            for givenElement in givenElements:
                nodeParents[childName].append(givenElement.text)

    return nodeValues, nodeParents


def countEdges(adjacencyList):
    """
    Generate basic statistics for the given edges.

    Args:
        adjacencyList (dict): Dictionary containing key:value pairs nodeID:parentNodeIDs.

    Returns:
        int: The total number of edges.
        int: The smallest in-degree value.
        int: The largest in-degree value.
        int: The average in-degree value.

    """
    maxInDegree = 0
    minInDegree = float('inf')

    totalNumberOfEdges = 0
    for parentGroup in adjacencyList.values():
        numberOfEdges = len(parentGroup)
        totalNumberOfEdges += numberOfEdges
        maxInDegree = max(maxInDegree, numberOfEdges)
        minInDegree = min(minInDegree, numberOfEdges)

    averageInDegree = totalNumberOfEdges / len(adjacencyList)

    return totalNumberOfEdges, minInDegree, maxInDegree, averageInDegree


def compareNodes(nodeValues1, nodeValues2):
    """
    Determine if the two given groups of node values are identical.

    Args:
        nodeValues1 (dict): Dictionary containing key:value pairs nodeID:nodeValues.
        nodeValues2 (dict): Dictionary containing key:value pairs nodeID:nodeValues.

    Returns:
        int: 0 if the node values are identical, 1 otherwise.

    """
    returnCode = GOOD

    if nodeValues1 != nodeValues2:
        print("ERROR: The node values are different!")
        returnCode = ERROR
    else:
        print("GOOD: The node values appear to be the same.")

    return returnCode


def compareEdges(adjacencyList1, adjacencyList2):
    """
    Determine if the two given groups of node parents are identical.

    Args:
        nodeValues1 (dict): Dictionary containing key:value pairs nodeID:parentNodeIDs.
        nodeValues2 (dict): Dictionary containing key:value pairs nodeID:parentNodeIDs.

    Returns:
        int: 0 if the node parents are identical, 1 otherwise.

    """
    returnCode = GOOD

    if adjacencyList1 != adjacencyList2:
        print("ERROR: The node edges are different!")
        returnCode = ERROR
    else:
        print("GOOD: The node parents appear to be the same.")

    return returnCode


def compareFiles(file1, file2):
    """
    Check to see if the two given BIF files are identical in structure.

    Args:
        file1 (str): Path to one of the BIF files to compare.
        file2 (str): Path to the other BIF file to compare.

    Returns:
        int: 0 if the BIF files are identical, 1 otherwise.

    """
    nodes1, adjacencyList1 = extractStructureInformation(file1)
    nodes2, adjacencyList2 = extractStructureInformation(file2)

    returnCode = compareNodes(nodes1, nodes2)
    returnCode = max(returnCode, compareEdges(adjacencyList1, adjacencyList2))

    return returnCode


def displayGraphInformation(file):
    """
    Print out metadata for the structure of the given BIF file.

    Args:
        file (str): Path to the BIF file to display metadata for.

    Returns:
        None

    """
    # Extract the information from the graph.
    graphName = extractGraphname(file)
    nodes, adjacencyList = extractStructureInformation(file)
    totalEdges, minInDegree, maxInDegree, averageInDegree = countEdges(adjacencyList)

    # Print the graph information.
    print("Graph: {}\n".format(graphName))
    print("Total Number of Nodes: {}".format(len(nodes)))
    print("Total Number of Edges: {}".format(totalEdges))
    print("Max Number of Parents: {}".format(maxInDegree))
    print("Min Number of Parents: {}".format(minInDegree))
    print("Avg Number of Parents: {}".format(averageInDegree))


def parseCommandLineArguments():
    """
    Parse the parameters from the commandline.

    """
    argument_parser = ArgumentParser(
        description="Script to help analyze BIF files.",
        add_help=False
    )

    mutually_exclusive_group = argument_parser.add_mutually_exclusive_group()
    mutually_exclusive_group.add_argument(
        '-a',
        '--analyze',
        metavar='BIF',
        help="Display metadata for the specified BIF file."
    )
    mutually_exclusive_group.add_argument(
        '-c',
        '--compare',
        metavar=('BIF1', 'BIF2'),
        nargs=2,
        help="Find any differences between the two specified BIF files."
    )
    argument_parser.add_argument(
        '-h',
        '--help',
        action='help',
        help="Show this message and exit."
    )

    return argument_parser.parse_args()


def main():
    arguments = parseCommandLineArguments()

    returnCode = GOOD

    if arguments.compare:
        returnCode = compareFiles(arguments.compare[0], arguments.compare[1])
    elif arguments.analyze:
        displayGraphInformation(arguments.analyze)

    exit(returnCode)


if __name__ == '__main__':
    main()