# Micro-Lesson 03 — Compose Word Transformations

## Question

How can we trim words, discard empty results, lowercase the survivors, and return their values in order without changing the source list?

## What you will learn

- Combine already-understood functions into a sequence of transformations.
- Distinguish changing a value with map from keeping/discarding it with filter.
- Trace each word through a lazy stream without imagining intermediate lists.

## Lesson status and prerequisites

- Learning status: `TODO`; prepared, not activated or understood.
- Primary idea: Combine already-understood functions into a sequence of transformations.
- New terms: stream, map, filter
- Prerequisite: [Pass Behavior as a Value](../02-functions-as-values/README.md), including lambdas and method references. Java 16+ is required for Stream.toList; the project runtime supports it.

## Start with the concept before the API

We already know how to supply a calculation. Now we want to combine several small calculations without hand-writing a new loop for every sequence. A **stream** describes a traversal of values from a source; it is not another list storing all those values. **map** supplies a function that changes each incoming value. **filter** supplies a boolean test: true keeps the value; false discards it.

Choose actions in learner language first: remove surrounding spaces → keep only non-empty values → lowercase what remains → gather results in order. Each calculation is independent of earlier words and changes no source data. The stream chain expresses those choices; it does not replace the need to choose them.

Source creation and map/filter calls prepare the traversal. Processing starts when the result-gathering operation `toList()` is invoked. In this sequential example a word flows through its applicable operations before the next word; there is no actual trim-all list, filter-all list, or lowercase-all list allocated by these stages. The tables below show the values as if observing one element, not exposed internal list storage. See [Java’s stream model](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/package-summary.html).

## Input and a concrete problem

Input is a non-null list of non-null strings: `[" Alice ", "", "BOB", "   ", "Chloë"]`. Trim leading/trailing characters handled by `String.trim` (including the ordinary spaces in this example), discard empty results, lowercase with `Locale.ROOT`, and preserve order. Expected values are `["alice", "bob", "chloë"]`: trim `" Alice "` to `"Alice"`, then lowercase to `"alice"`; discard `""` and the space-only word after trimming; lowercase `"BOB"` to `"bob"` and `"Chloë"` to `"chloë"`.

## Block-by-block code walkthrough

Original snapshots are retained. Where a value changes several times, its cell shows the sequence ending at the final value.

### Block 1 — Read from the source without editing it

```java
return words.stream()
```

`words` refers to the input list. `stream()` creates a sequential traversal recipe; it neither copies nor edits the input list and does not yet process a word. The leading `return` returns the final expression once all chained operations finish; it does not return a partial stream here. Without a source there are no values to process. A clearer reading is “start with words,” then follow the dots below.

### Block 2 — Describe how each word changes

```java
.map(String::trim)
```

`String::trim` is the method-reference form of `word -> word.trim()`, using the syntax learned in lesson 2. It changes the current value, not the source list entry. For `" Alice "`, the value becomes `"Alice"`; for `"   "`, it becomes `""`. String values cannot be edited in place by this call; Java may reuse an unchanged String object, so do not infer a fresh object on every invocation. Omitting trimming would leave spaces and prevent whitespace-only strings from being discarded by the next test.

### Block 3 — Keep or discard the transformed value

```java
.filter(word -> !word.isEmpty())
```

This lambda receives the already-trimmed word. `isEmpty()` returns true only when its length is zero; `!` reverses it, making filter’s keep decision. For `"Alice"`, **❌ `word.isEmpty()`:** false; **✅ `!word.isEmpty()`:** `!false → true`, so keep it. For `""`, **✅ `word.isEmpty()`:** true; **❌ `!word.isEmpty()`:** `!true → false`, so discard it and skip later operations for that word. Filtering before trimming would keep `"   "`, since its original length is three. Equivalent: `word -> word.length() > 0`.

### Block 4 — Normalize surviving values predictably

```java
.map(word -> word.toLowerCase(Locale.ROOT))
```

For each survivor, calculate a lowercased String value. `Locale.ROOT` supplies language-neutral case rules explicitly so the transformation is independent of the machine’s default language settings. It is not a whitespace operation. For `"BOB"`, the result is `"bob"`. Without this map the output would still contain uppercase letters. Equivalent: a named helper that calls `toLowerCase(Locale.ROOT)`, referenced by name. Unicode/language-specific text rules can be studied later; `trim` is not a universal Unicode-whitespace remover.

### Block 5 — Run the recipe and gather the final values

```java
.toList();
```

This call starts the traversal, keeps source order, and produces an unmodifiable result list. It does not promise a fresh list identity for every equal result, so compare values with equals rather than `==`. There is no externally accessible output list being appended by your code; the “collected values so far” below are a conceptual sequential trace. Without a result-gathering operation, the declared List-returning method would not return the required list. If callers need a modifiable list, explicitly copy the finished result into `new ArrayList<>(result)` outside this method’s contract. [Stream.toList contract](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/Stream.html#toList()).

**Word 0: input `" Alice "`.**

- `map(String::trim)` reads word `" Alice "`; `word.trim()` returns `"Alice"`.
- **❌ `word.isEmpty()`:** `false`; **✅ filter `!word.isEmpty()`:** `!false → true`.
- The filter keeps `"Alice"`; `map(word -> word.toLowerCase(Locale.ROOT))` returns `"alice"`.
- `toList()` includes this value; conceptual output: `[] → ["alice"]`.
- The stream library continues to the next word; the source list stays unchanged.

| source position (explanatory, not a Java index variable) | original | after trim | isEmpty | filter keep | lowercase | conceptual collected values | source after |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `0` | `" Alice "` | `"Alice"` | `❌ false` | `✅ true` | `"alice"` | `["alice"]` | `[" Alice ", "", "BOB", "   ", "Chloë"]` |

**Word 1: input `""`.**

- `map(String::trim)` reads word `""`; `word.trim()` returns `""`.
- **✅ `word.isEmpty()`:** `true`; **❌ filter `!word.isEmpty()`:** `!true → false`.
- The filter discards `""`; lowercase is `Skipped`, with no emitted value. Conceptual output stays `["alice"]`.
- The stream library continues to the next word; the source list stays unchanged.

| source position (explanatory, not a Java index variable) | original | after trim | isEmpty | filter keep | lowercase | conceptual collected values | source after |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `1` | `""` | `""` | `✅ true` | `❌ false` | `Skipped` | `["alice"]` | `[" Alice ", "", "BOB", "   ", "Chloë"]` |

**Word 2: input `"BOB"`.**

- `map(String::trim)` reads word `"BOB"`; `word.trim()` returns `"BOB"`.
- **❌ `word.isEmpty()`:** `false`; **✅ filter `!word.isEmpty()`:** `!false → true`.
- The filter keeps `"BOB"`; `map(word -> word.toLowerCase(Locale.ROOT))` returns `"bob"`.
- `toList()` includes this value; conceptual output: `["alice"] → ["alice", "bob"]`.
- The stream library continues to the next word; the source list stays unchanged.

| source position (explanatory, not a Java index variable) | original | after trim | isEmpty | filter keep | lowercase | conceptual collected values | source after |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `2` | `"BOB"` | `"BOB"` | `❌ false` | `✅ true` | `"bob"` | `["alice", "bob"]` | `[" Alice ", "", "BOB", "   ", "Chloë"]` |

**Word 3: input `"   "`.**

- `map(String::trim)` reads word `"   "`; `word.trim()` returns `""`.
- **✅ `word.isEmpty()`:** `true`; **❌ filter `!word.isEmpty()`:** `!true → false`.
- The filter discards `""`; lowercase is `Skipped`, with no emitted value. Conceptual output stays `["alice", "bob"]`.
- The stream library continues to the next word; the source list stays unchanged.

| source position (explanatory, not a Java index variable) | original | after trim | isEmpty | filter keep | lowercase | conceptual collected values | source after |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `3` | `"   "` | `""` | `✅ true` | `❌ false` | `Skipped` | `["alice", "bob"]` | `[" Alice ", "", "BOB", "   ", "Chloë"]` |

**Word 4: input `"Chloë"`.**

- `map(String::trim)` reads word `"Chloë"`; `word.trim()` returns `"Chloë"`.
- **❌ `word.isEmpty()`:** `false`; **✅ filter `!word.isEmpty()`:** `!false → true`.
- The filter keeps `"Chloë"`; `map(word -> word.toLowerCase(Locale.ROOT))` returns `"chloë"`.
- `toList()` includes this value; conceptual output: `["alice", "bob"] → ["alice", "bob", "chloë"]`.
- `toList()` reaches the end and the return expression returns `["alice","bob","chloë"]`; the source list stays unchanged.

| source position (explanatory, not a Java index variable) | original | after trim | isEmpty | filter keep | lowercase | conceptual collected values | source after |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `4` | `"Chloë"` | `"Chloë"` | `❌ false` | `✅ true` | `"chloë"` | `["alice", "bob", "chloë"]` | `[" Alice ", "", "BOB", "   ", "Chloë"]` |

After source position `4`, there is no next source element. There is no application-level `index++` or loop condition in this reference: the stream library controls traversal. `toList` completes, and the return expression returns `["alice", "bob", "chloë"]`. No hidden lowercase invocation is owed to discarded words.

## Complete reference solution

The original runnable files are preserved at their existing paths: [NormalizeWordsSolution.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/puretransformations/NormalizeWordsSolution.java).

```java
// Input: words=[" Alice ","","BOB","   ","Chloë"]
// Expected return: ["alice","bob","chloë"]

static List<String> normalizeWords(List<String> words) {
        return words.stream()
                .map(String::trim)
                .filter(word -> !word.isEmpty())
                .map(word -> word.toLowerCase(Locale.ROOT))
                .toList();
    }
```

## Correctness, costs, and limits

Each emitted value comes from one source position: trimmed, accepted only if non-empty, then lowercased. Rejected values contribute nothing; sequential collection preserves survivor order. None of the callbacks writes to the source list or any outside object. That gives an input-preserving, predictable transformation under the stated contract.

Let `n` be the number of words and `c` the total input characters. Work is `O(n+c)` including visits to empty strings; let `d` be the total output characters, giving `O(n+d)` output space including list entries. This distinction matters for many empty strings, where `c=0` but there are still `n` visits. There are no full intermediate lists between the shown stream stages.

A plain loop with an explicitly owned result list can implement the same rules and is a good alternative when branching or debugging is clearer that way. Writing into the supplied list or secretly appending into an outside list from a callback breaks this transformation’s side-effect boundary. Do not assume a stream makes such code pure.

For `words=[]`, no source element exists, so none of the functions runs; the result is an empty unmodifiable list. For `["already"]`, trim leaves the value unchanged, keep is true, and lowercase leaves it unchanged; result `["already"]`. A null list or null element violates the contract and is not silently discarded.

## Practice after learning the concept

Implement [NormalizeWordsPractice.java](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/puretransformations/NormalizeWordsPractice.java) in IntelliJ, then run its main. The original practice program is preserved. The [reference solution](../../../../../../src/test/java/com/instagram/backend/interview/functionalprogramming/puretransformations/NormalizeWordsSolution.java) checks values, order, and input preservation. List modifiability follows the API contract above and is verified by the supplemental track checks.

## Questions and explained answers

<details>
<summary>1. Does map first create a list of all trimmed words?</summary>

No. The map call adds a processing step. Once toList runs, each sequential element flows through its applicable steps; the tables are observations, not allocated intermediate lists.

</details>

<details>
<summary>2. Why trim before filtering, and what is skipped for a blank word?</summary>

Three spaces are not an empty String. Trim first creates the empty value; the keep test becomes false, so lowercase is skipped and that word contributes no output.

</details>

<details>
<summary>3. Does an unmodifiable result mean we modified or froze the input list?</summary>

No. The result list cannot be edited through its returned API, but the original list was never modified or frozen. These are separate properties.

</details>

## Stop/go

Stay TODO until your practice passes and you can narrate a blank and a surviving word, explain when processing starts, distinguish source preservation from output modifiability, and answer the questions. Do not advance because the artifact or renderer works.
