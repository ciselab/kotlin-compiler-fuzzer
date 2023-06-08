class Classifier:

    def __init__(self, name, classifier):
        self.name = name
        self.cls = classifier
        
    def fit(self, x, y):
        self.cls = self.cls.fit(x, y)
    
    def __call__(self, x):
        return self.cls.predict(x)
    
    def __repr__(self):
        return f"Classifier object of type {self.name}"