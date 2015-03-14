# Introduction #

As Record Linkage (RL) is used in many applications such as data cleansing, spelling corrections and ... We explore an open source package, called fuzzyJoin released by Vernica from UCI. The goal of this project is to eventually do the following:
  * Explain the open source of fuzzyJoin in details.
  * Explain our C++ implementation to do fuzzyJoin.
  * Add our java implementation of RL.

# Details #
> Record linkage (RL) is the problem of finding similarity measure among different entities with respect to a set of attributes. This problem is also referred to as fuzzyJoin in Vernica opened source  package. Our work is to explain the project in details. Meaning, to present a deeper over view of the the figures shown in his SIGMOD2010 paper. Then, implement the same class using C++ and embed it to Hadoop pipes to run it. Finally, we add our own RL implementation and compare its performance using java-based Hadoop.

Contribution:
  * Materialize the bottle neck for record linkage on MapReduce under the three algorithms.
  * Show speedup/scaleup comparison of the three implementations.
  * MAYBE add our RL implementation including:
    * a. tf-idf documents conversion to Hadoop sequence files.
    * b. Static filtering  to reduce comparison instead of the prefix filtering and the grouping method of SIGMOD 2010.

Benchmarks:
  * dblp sample of 100 records replicated as needed.

# References #

  * Efficient set similarity joins using MapReduce.Vernica,SIGMOD2010  - used for its package
  * Efficient Record Linkage in Large Data Sets - used for definition of record linkage.
  * ?   - so many options for this one - refers to similarity search.

