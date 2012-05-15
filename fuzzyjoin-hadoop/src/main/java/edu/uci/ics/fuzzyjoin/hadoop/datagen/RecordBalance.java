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

package edu.uci.ics.fuzzyjoin.hadoop.datagen;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.GenericOptionsParser;

import edu.uci.ics.fuzzyjoin.hadoop.FuzzyJoinDriver;

public class RecordBalance {
    public static void main(String[] args) throws IOException {
        //
        // setup job
        //
        JobConf job = new JobConf();
        new GenericOptionsParser(job, args);
        job.setJobName(RecordBalance.class.getSimpleName());

        job.setMapperClass(MapRecordOnly.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);

        //
        // set input & output
        //
        String dataDir = job.get(FuzzyJoinDriver.DATA_DIR_PROPERTY);
        if (dataDir == null) {
            throw new UnsupportedOperationException("ERROR: "
                    + FuzzyJoinDriver.DATA_DIR_PROPERTY + " not set");
        }
        String suffix = job.get(FuzzyJoinDriver.DATA_SUFFIX_INPUT_PROPERTY, "");
        if (!suffix.isEmpty()) {
            if (suffix.indexOf(FuzzyJoinDriver.SEPSARATOR) >= 0) {
                throw new UnsupportedOperationException("ERROR: "
                        + FuzzyJoinDriver.DATA_SUFFIX_INPUT_PROPERTY
                        + " contains more that one value");
            }
            suffix = "." + suffix;
        }
        String recordsBulk = "/recordsbulk" + suffix;
        int dataCopy = job.getInt(FuzzyJoinDriver.DATA_COPY_PROPERTY, 1);
        for (int i = 0; i < dataCopy; i++) {
            FileInputFormat.addInputPath(job, new Path(dataDir + recordsBulk
                    + String.format("-%03d", i)));
        }
        Path outputPath = new Path(dataDir + "/records" + suffix
                + String.format("-%03d", dataCopy - 1));
        FileOutputFormat.setOutputPath(job, outputPath);
        FileSystem.get(job).delete(outputPath, true);

        //
        // run
        //
        FuzzyJoinDriver.run(job);
    }
}
