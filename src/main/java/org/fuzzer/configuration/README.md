# Configuration contents

One can configure the fuzzer using a `yaml` file. A sample configuration is provided in the root of the repository, under `config.yaml`. A configuration is composed of the following fields:

- `heuristic`: Contains information about the search heuristic that the fuzzer uses. Contains the following subfields:
  - `type`: One of `random`, `diversity-ga`, or `moga`. The `random` option will use random search. The `diversity-ga` will optimize for file diversity using a genetic algorithm (GA). `moga` uses a many-objective GA that attempts to minimize the file size while simultaneously maximizing the number of each available language structure. The remainder of the fields in this list govern the two GAs:
  - `population`: The integer population size.
  - `distance-metric`: Either `euclidean` or `manhattan`, which signals the distance metric to use for diversity heuristic (when relevant).
  - `new-blocks-genreated`: The integer number of new blocks to generate during each iteration of the GA.
  - `selection`: A subfield that governs the selection method for GAs. It contains the following subfields:
    - `maximum-length`: The integer maximum length that files must not exceed to be selected (across all selection methods).
    - `mo-selection`: One of `domination-count` or `domination-rank`, to be used in many-objective GAs.
    - `so-selection`: One of `tournament` or `truncated`, to be used in single-objective GAs and as part of the `domination-count` method.
    - `tournament-size`: The integer size of tournaments to use in selection (if enabled).
    - `tournament-selection-probability`: The double probability used to select the best candidate in a tournament (if enabled).
    - `truncation-proportion`: The double proportion of solutions to retain in the truncation selection method (if enabled).
- `grammar`: Contains information about the syntactic sampling strategies. Contains the following fields:
  - `simplicity-bias`: The probability with which to sample simple statements and expressions.
  - `plus-node-dist` and `star-node-dist` govern the `+` and `*` regular-expression sampling distributions. Each distribution must contain the following fields:
    - `type`: one of `uniform` or `geometric` for the corresponding distribution.
    - `lower-bound`: the integer lower bound of the distribution.
    - `upper-bound`: the integer upper type of the distribution (only used for uniform at the moment).
  - `func-stmts-dist`, `do-while-stmts-dist`, `loop-stmts-dist`, `try-block-dist`, `catch-block-dist`, `finally-block-dist`, `if-block-dist`, and `else-block-dist` allow the same customization as `plus-node-dist` and `star-node-dist`, for their respective statement blocks.
  - `simple-expr-dist`, `if-expr-dist`, `elvis-expr-dist`, and `try-catch-expr-dist` allow the same customization as `plus-node-dist` and `star-node-dist`, for their respective expression blocks.
    - `else-block-dist` and `finally-block-dist` contain an additional `probability` field, which signals the likelihood of sampling such a block in the first place.
- `language-features`: Contains information about the frequency with which to favor individual language features. Contains the following fields:
  - `functions`, with the subfield `enabled`, which should always be set to `true`.
  - `expressions`: Contains the frequency information about expressions. Expressions have the following subfields: `simple-expr`, `if-expr`, `elivs-expr`, and `try-catch-expr`, for their corresponding Kotlin language features. Each expression contains the following fields:
    - `enable`: Either `true` or `false`. Signals whether to use the language feature during sampling.
    - `weight`: A double that signals the frequency with which the feature will be sampled. Weights are normalized during parsing. If `enable` is set to `false`, the `weight` of that language feature is discarded.
  - `statements`: Behaves the same as `expressions`, and has the following subfields: `simple-stmt`, `do-while-stmt`, and `assignment-stmt`.

## Sample configuration

Below is an example of a configuration file that contains examples of most feasible combinations of inputs:

```yaml
config:
  heuristic:
    type: moga
    population-size: 40
    new-blocks-generated: 20
    distance-metric: euclidean
    selection:
      mo-selection: domination-count
      so-selection : tournament
      tournament-size: 4
      tournament-selection-probability: 0.75
      truncation-proportion: 0.3
      maximum-length: 500
  grammar:
    simplicity-bias: 0.6
    plus-node-dist:
      type: geometric
      lower-bound: 1
    star-node-dist:
      type: geometric
      lower-bound: 0
    func-stmts-dist:
      type: geometric
      lower-bound: 0
    func-params-dist:
      type: geometric
      lower-bound: 0
    do-while-stmts-dist:
      type: geometric
      lower-bound: 0
    loop-stmts-dist:
      type: geometric
      lower-bound: 0
    try-block-dist:
      type: geometric
      lower-bound: 0
    catch-block-dist:
      type: geometric
      lower-bound: 0
    catch-block-stmt-dist:
      type: geometric
      lower-bound: 0
    finally-block-dist:
      probability: 0.5
      type: geometric
      lower-bound: 0
    if-block-dist:
      type: geometric
      lower-bound: 0
    else-block-dist:
      # The else block probability is discarded at the moment.
      probability: 1.0
      type: geometric
      lower-bound: 0
  language-features:
    functions:
      enable: true
    expressions:
      simple-expr:
        enable: true
        weight: 1.0
      if-expr:
        enable: true
        weight: 1.0
      elvis-expr:
        enable: true
        weight: 1.0
      try-catch-expr:
        enable: true
        weight: 1.0
    statements:
      simple-stmt:
        enable: true
        weight: 1.0
      do-while-stmt:
        enable: true
        weight: 1.0
      assignment-stmt:
        enable: false
        weight: 3.0
      loop-stmt:
        enable: true
        weight: 5.5
```