/**
 * Copyright 2010-2011 The Regents of the University of California
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS"; BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under
 * the License.
 * 
 * Author: Rares Vernica <rares (at) ics.uci.edu>
 */

package edu.uci.ics.fuzzyjoin.hadoop.ridpairs.ppjoin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import edu.uci.ics.fuzzyjoin.FuzzyJoinConfig;
import edu.uci.ics.fuzzyjoin.FuzzyJoinMemory;
import edu.uci.ics.fuzzyjoin.ResultJoin;
import edu.uci.ics.fuzzyjoin.hadoop.ridpairs.ValueJoin;

/**
 * @author rares
 * 
 *         KEY1: group, lenght, relation
 * 
 *         VALUE1: relation, RID, length, tokens
 * 
 *         KEY2: RID pair and similarity
 * 
 *         VALUE2: unused
 */
public class ReduceJoin extends MapReduceBase implements
        Reducer<Object, ValueJoin, Text, NullWritable> {

    private float similarityThreshold;
    private final Text outputKey = new Text();
    private final NullWritable outputValue = NullWritable.get();

    @Override
    public void configure(JobConf job) {
        similarityThreshold = job.getFloat(
                FuzzyJoinConfig.SIMILARITY_THRESHOLD_PROPERTY,
                FuzzyJoinConfig.SIMILARITY_THRESHOLD_VALUE);
    }

    public void reduce(Object unused, Iterator<ValueJoin> inputValues,
            OutputCollector<Text, NullWritable> output, Reporter reporter)
            throws IOException {
        // System.out.println("R:Key:" + unused);
        HashMap<Integer, Integer> rids = new HashMap<Integer, Integer>();
        FuzzyJoinMemory fuzzyJoinMemory = new FuzzyJoinMemory(
                similarityThreshold);
        int crtRecordId = 0;
        int lastLength = -1;

        while (inputValues.hasNext()) {
            ValueJoin recordInfo = inputValues.next();
            int relation = recordInfo.getRelation();
            int rid = recordInfo.getRID();
            int length = recordInfo.getLength();
            int[] reccordTrimmed = recordInfo.getTokens();
            int[] record;
            if (length == reccordTrimmed.length) {
                record = reccordTrimmed;
            } else {
                record = new int[length];
                int i = 0;
                for (; i < reccordTrimmed.length; i++) {
                    record[i] = reccordTrimmed[i];
                }
                // pad with non-existing tokens (for suffix filter)
                for (; i < record.length; i++) {
                    record[i] = Integer.MAX_VALUE;
                }
            }

            if (relation == 0) {
                //
                // build
                //
                // System.out.println("R:Build:" + recordInfo);
                if (lastLength != -1) {
                    fuzzyJoinMemory.prune(lastLength);
                    lastLength = -1;
                }
                fuzzyJoinMemory.add(record);
                rids.put(crtRecordId, recordInfo.getRID());
                crtRecordId++;
            } else {
                //
                // probe
                //
                // System.out.println("R:Probe:" + recordInfo);
                ArrayList<ResultJoin> results = fuzzyJoinMemory.join(record,
                        length);
                for (ResultJoin result : results) {
                    outputKey.set("" + rids.get(result.index)
                            + FuzzyJoinConfig.RIDPAIRS_SEPARATOR + rid
                            + FuzzyJoinConfig.RIDPAIRS_SEPARATOR
                            + result.similarity);
                    output.collect(outputKey, outputValue);
                    // System.out.println("R:Output:" + outputKey);
                }
                lastLength = length;
            }
        }
    }
}
