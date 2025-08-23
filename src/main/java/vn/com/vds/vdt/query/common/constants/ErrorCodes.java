package vn.com.vds.vdt.query.common.constants;

@SuppressWarnings("all")
public class ErrorCodes {

    // ---------------- System / Common ---------------- (QS0xxxx)
    public static final String QS00001 = "QS00001"; // Success
    public static final String QS00002 = "QS00002"; // Invalid request
    public static final String QS00003 = "QS00003"; // Unknown error
    // GAP reserved for future system-level errors: QS00004 ~ QS00050

    // ---------------- Validation Errors -------------- (QS1xxxx)
    public static final String QS10001 = "QS10001"; // Missing required field
    public static final String QS10002 = "QS10002"; // Invalid field format
    public static final String QS10003 = "QS10003"; // Unsupported type
    // GAP reserved: QS10004 ~ QS10050

    // ---------------- Parser Errors ------------------ (QS2xxxx)
    public static final String QS20001 = "QS20001"; // Parse error
    public static final String QS20002 = "QS20002"; // Parser not found
    // GAP reserved: QS20003 ~ QS20050

    // ---------------- Database Errors ---------------- (QS3xxxx)
    public static final String QS30001 = "QS30001"; // DB connection failed
    public static final String QS30002 = "QS30002"; // DB query execution error
}