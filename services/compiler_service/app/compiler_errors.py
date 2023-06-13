from enum import Enum

class CompilerVariant(Enum):
    NEITHER = 0,
    K1 = 1,
    K2 = 2,
    BOTH = 3

class CompilerError(Enum):
    NONE = 0,
    OOM = 1,
    RESOLUTION_AMBIGUITY = 2,
    CONFLICTING_OVERLOADS = 3,
    IR = 4,
    OTHER = 5,
