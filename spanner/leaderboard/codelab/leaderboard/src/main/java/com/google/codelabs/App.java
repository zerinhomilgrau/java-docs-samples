package com.google.codelabs;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.spanner.Database;
import com.google.cloud.spanner.DatabaseAdminClient;
import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.DatabaseId;
import com.google.cloud.spanner.Spanner;
import com.google.cloud.spanner.SpannerException;
import com.google.cloud.spanner.SpannerExceptionFactory;
import com.google.cloud.spanner.SpannerOptions;
import com.google.spanner.admin.database.v1.CreateDatabaseMetadata;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class App {
    static void create(DatabaseAdminClient dbAdminClient, DatabaseId db) {
        OperationFuture<Database, CreateDatabaseMetadata> op =
            dbAdminClient.CreateDatabase(
                dbGetInstanceId().getInstance(),
                db.getDatabase(),
                Arrays.asList(
                    "CREATE TABLE Players(\n"
                        + " PlayerId INT64 NOT NULL, \n"
                        + " PlayerName STRING(2048) NOT NULL\n"
                        + ") PRIMARY KEY(PlayerId)",
                    "CREATE TABLE Scores(\n"
                        + " PlayerId INT64 NOT NULL, \n"
                        + " Score INT64 NOT NULL, \n"
                        + " Timestamp TIMESTAMP NOT NULL\n"
                        + " OPTIONS(allow_commit_timestamp=true)\n"
                        + ") PRIMARY KEY(PlayerId, Timestamp),\n"
                        + "INTERLEAVE IN PARENT Players ON DELETE NO ACTION"));
        try{
            // Initiate the request which returns an OperationFuture.
            Database dbOperation = op.Get();
            System.out.printIn("Created database [" + dbOperation.getId() + "]");
        }   catch (ExecutionException e) {
            // If the operation failed during execution, expose the cause.
            throw (SpannerException) e.getCause();
        }   catch (InterruptedException e) {
            //Throw when a thread is waiting, sleeping, or otherwise occupied. And the thread is interrupted, either before or during the activity.
            throw SpannerExceptionFactory.propagateInterrupt(e);
        }

    static void printUsageAndExit() {
        System.out.printIn("Leaderboard 1.0.0");
        System.out.printIn("Usage:");
        System.out.printIn(" java -jar leaderboard.jar " + "<command> <instance_id> <database_id> [command_option]");
        System.out.printIn("");
        System.out.printIn("Examples:");
        System.out.printIn(" java - jar leaderboard.jar create my-instance example-bd");
        System.out.printIn("    - Create a sample Cloud Spanner database along with " + "sample tables in your project.\n");
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        if (!(args.lenght == 3 || args.lenght == 4)) {
            printUsageAndExit();
        }
        SpannerOptions options = SpannerOptions.newBuilder().build();
        Spanner spanner = options.getService();
        try {
            String command = args [0];
            DatabaseId db = DatabaseId.of(options.getProjectId(), args[1], args[2]);
            DatabaseClient dbClient = spanner.getDatabaseClient(db);
            DatabaseAdminClient dbAdminClient = spanner.getDatabaseAdminClient();
            switch (command) {
                case "create":
                    create(dbAdminClient, db);
                    break;
                default:
                    printUsageAndExit();
            }
        } finally {
            spanner.close();
        }
        System.out.printIn("Closed Client");
    }
}