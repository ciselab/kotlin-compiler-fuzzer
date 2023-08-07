# Configuration contents

One can configure the fuzzer using a `yaml` file. A sample configuration is provided in the root of the repository, under `config.yaml`. A configuration is composed of the following fields:

- `heuristic`: Contains information about the search heuristic that the fuzzer uses. Contains the following subfields:
  - `type`: One of `random`, `diversity-ga`, `proximity-ga`, `structure-moga`, `proximity-moga`, or `proximity-wts`. The `random` option will use random search. The `diversity-ga` will optimize for file diversity using a genetic algorithm (GA). `moga` uses a many-objective GA that attempts to minimize the file size while simultaneously maximizing the number of each available language structure. The two following options are analogous for the proximity heuristic, while the last implements the Whole-Suite approach by Arcuri and Fraser. The remainder of the fields in this list govern the two GAs:
  - `population`: The integer population size.
  - `distance-metric`: Either `euclidean` or `manhattan`, which signals the distance metric to use for diversity heuristic (when relevant).
  - `new-blocks-genreated`: The integer number of new blocks to generate during each iteration of the GA.
  - `selection`: A subfield that governs the selection method for GAs. Selection can be performed on the `individual` or `suite` level, to set apart the WS approach. Both `individual` and `suite` are subfields of `selection`, and they themselves contain the following subfields:
    - `maximum-length` (`individual`): The integer maximum length that files must not exceed to be selected (across all selection methods).
    - `mo-selection` (`individual`): One of `domination-count` or `domination-rank`, to be used in many-objective GAs.
    - `so-selection` (`individual` and `suite`): One of `tournament` or `truncated`, to be used in single-objective GAs and as part of the `domination-count` method.
    - `tournament-size` (`individual` and `suite`): The integer size of tournaments to use in selection (if enabled).
    - `tournament-selection-probability`(`individual` and `suite`): The double-valued probability used to select the best candidate in a tournament (if enabled).
    - `truncation-proportion` (`individual` and `suite`): The double-valued proportion of solutions to retain in the truncation selection method (if enabled).
  - `remote`: a subfield that stores information regarding the addresses of other services, generally ran in containers on the local network. Contains the following subfields:
    - `embedding-single` and `embedding-multi`, the addresses of the embedding service API. This service transofrms code into a vectorized numerical representation that the proximity heuristics require.
    - `targets`, the location of the targets (or clustering) service API. This component gives the locations of the targets for the proximity heuristics.
    - `num-targets`: the integer-valued number of targets to expect (as to avoid misconfiguration).
    - `oom`, a field that contains information regarding the out-of-memory classifier API. `oom` contains three subfields: `enable` (`true` or `false`), and `oom-single` and `oom-multi`, the provide analogous functionality to their `embedding` counterparts.
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
    type: proximity-moga
    population-size: 50
    new-blocks-generated: 10
    distance-metric: euclidean
    blocks-per-suite: 20
    num-iters-per-target: 10
    suite-mutation-probability: 0.1
    selection:
      individual:
        mo-selection: domination-rank
        so-selection : tournament
        tournament-size: 4
        tournament-selection-probability: 0.99
        truncation-proportion: 0.3
        maximum-length: 500
      suite:
        so-selection: tournament
        tournament-size: 10
        tournament-selection-probability: 0.99
        truncation-proportion: 0.3
    remote:
      embedding-single: http://localhost:9090/embedding-single/
      embedding-multi: http://localhost:9090/embedding-multiple/
      targets: http://localhost:9091/centers/kmeans_100/
      num-targets: 100
      oom:
        enable: false
        oom-single: http://localhost:9092/predict-single/
        oom-multi: http://localhost:9092/predict-multiple/
  grammar:
    simplicity-bias: 0.5
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
        enable: true
        weight: 1.0
      loop-stmt:
        enable: false
        weight: 1.0
```