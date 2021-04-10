import networkx as nx

graph = nx.read_edgelist("edgelist.txt", create_using=nx.DiGraph())
ranks = nx.pagerank(graph, alpha=0.85, personalization=None, max_iter=30, tol=1e-06, nstart=None, weight='weight', dangling=None)
f = open("external_pageRankFile.txt", "w")
for docID, rank in ranks.items():
    f.write("/home/lewis/latimes/latimes/" + docID + "=" + str(rank) + "\n")
