from sklearn.cluster import KMeans, AgglomerativeClustering, OPTICS, Birch

import numpy as np

class ClusteringAlgorithm:
    def __init__(self, name, clustering_algorithm) -> None:
        self.name = name
        self.cls = clustering_algorithm
        self.centers = None

    def fit(self, data):
        self.cls = self.cls.fit(data)
        self.centers = self.compute_centers(data)
    
    def compute_centers(self, data):
        return None

    def __repr__(self) -> str:
        return f"Clustering Algorithm {self.name} using {self.cls}"
    
class KMeansCls(ClusteringAlgorithm):
    def __init__(self, k, seed = 0) -> None:
        super().__init__("KMeans", KMeans(n_clusters=k, random_state=seed))

    def compute_centers(self, data):
        return self.cls.cluster_centers_

class AgglomerativeCls(ClusteringAlgorithm):
    def __init__(self, linkage, name = "Agglomerative") -> None:
        super().__init__(name, AgglomerativeClustering(linkage = linkage))
        
    def fit(self, data):
        self.cls.fit(data)
        self.centers = self.compute_centers(data)

    def compute_centers(self, data):
        self.cls = self.cls.fit(data)
        predictions = self.cls.labels_
        return np.array([np.mean(data[np.where(predictions == cluster_idx)], axis=0) for cluster_idx in np.unique(predictions)])

class OpticsCls(ClusteringAlgorithm):
    def __init__(self, min_samples, name = "OPTICS") -> None:
        super().__init__(name, OPTICS(min_samples=min_samples))
        
    def fit(self, data):
        self.cls.fit(data)
        self.centers = self.compute_centers(data)
    
    def compute_centers(self, data):
        predictions = np.array(self.cls.labels_[self.cls.labels_ != -1]) # Alg may ignore some data
        return np.array([np.mean(data[np.where(predictions == cluster_idx)], axis=0) for cluster_idx in np.unique(predictions)])

class BirchCls(ClusteringAlgorithm):
    def __init__(self) -> None:
        super().__init__(f"Birch", Birch(n_clusters=None))
    
    def compute_centers(self, data):
        return self.cls.subcluster_centers_  
