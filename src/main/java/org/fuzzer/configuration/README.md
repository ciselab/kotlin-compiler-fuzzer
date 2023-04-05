# Configuration contents

One can configure the fuzzer using a `yaml` file. A sample configuration is provided in the root of the repository, under `config.yaml`. A configuration is composed of the following fields:

- `heuristic`: Contains information about the search heuristic that the fuzzer uses. Contains the following subfields:
  - `type`: One of `random` or `diversity-ga`. The `random` option will use random search. The `diversity-ga` will optimize for file diversity using a genetic algorithm. The following fields govern the GA:
    - `population`: The integer population size.
    - `tournament-size`: The integer size of tournaments to use in selection.
    - `selection-probability`: The double probability used to select the best candidate.
    - `maximum-length`: The integer maximum length that files must not exceed to be selected.
    - `new-blocks-genreated`: The integer number of new blocks to generate during each iteration of the GA.
- `grammar`: Contains information about the syntactic sampling strategies. Contains the following fields:
  - `simplicity-bias`: The probability with which to sample simple statements and expressions.
  - `plus-node-dist` and `star-node-dist` govern the `+` and `*` regular-expression sampling distributions. Each distribution must contain the following fields:
    - `type`: one of `uniform` or `geometric` for the corresponding distribution.
    - `lower-bound`: the integer lower bound of the distribution.
    - `upper-bound`: the integer upper type of the distribution (only used for uniform at the moment).
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
    type: diversity-ga
    population-size: 40
    tournament-size: 4
    selection-probability: 0.75
    maximum-length: 500
    new-blocks-generated: 20
    distance-metric: euclidean
  grammar:
    simplicity-bias: 0.4
    plus-node-dist:
        type: uniform
        lower-bound: 1
        upper-bound: 2
    star-node-dist:
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