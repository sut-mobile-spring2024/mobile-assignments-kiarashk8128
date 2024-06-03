package com.example.xo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version and Name
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "xoGame.db";

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_GAME_STATE = "game_state";

    // Users Table Columns
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_DATE_OF_BIRTH = "date_of_birth";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_DEVICE_UUID = "device_uuid";

    // Game State Table Columns
    public static final String COLUMN_GAME_ID = "game_id";
    public static final String COLUMN_USER_ID_FK = "user_id_fk";
    public static final String COLUMN_BOARD_STATE = "board_state";
    public static final String COLUMN_PLAYER_TURN = "player_turn";
    public static final String COLUMN_ROUND_COUNT = "round_count";
    public static final String COLUMN_PLAYER1_SYMBOL = "player1_symbol";
    public static final String COLUMN_PLAYER2_SYMBOL = "player2_symbol";
    public static final String COLUMN_COMPUTER_SYMBOL = "computer_symbol";
    public static final String COLUMN_IS_FINISHED = "is_finished";

    public static final String TABLE_GAME_HISTORY = "game_history";
    public static final String COLUMN_HISTORY_ID = "history_id";
    public static final String COLUMN_GAME_DATE = "game_date";
    public static final String COLUMN_RESULT = "result";

    // Create Table Statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USERNAME + " TEXT,"
            + COLUMN_EMAIL + " TEXT,"
            + COLUMN_DATE_OF_BIRTH + " TEXT,"
            + COLUMN_GENDER + " TEXT,"
            + COLUMN_PASSWORD + " TEXT,"
            + COLUMN_DEVICE_UUID + " TEXT"
            + ")";

    private static final String CREATE_TABLE_GAME_STATE = "CREATE TABLE " + TABLE_GAME_STATE + "("
            + COLUMN_GAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER,"
            + COLUMN_BOARD_STATE + " TEXT,"
            + COLUMN_PLAYER_TURN + " INTEGER,"
            + COLUMN_ROUND_COUNT + " INTEGER,"
            + COLUMN_PLAYER1_SYMBOL + " TEXT,"
            + COLUMN_PLAYER2_SYMBOL + " TEXT,"
            + COLUMN_COMPUTER_SYMBOL + " TEXT,"
            + COLUMN_IS_FINISHED + " INTEGER DEFAULT 0,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
            + ")";

    private static final String CREATE_TABLE_GAME_HISTORY = "CREATE TABLE " + TABLE_GAME_HISTORY + "("
            + COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_ID_FK + " INTEGER,"
            + COLUMN_GAME_DATE + " TEXT,"
            + COLUMN_PLAYER1_SYMBOL + " TEXT,"
            + COLUMN_PLAYER2_SYMBOL + " TEXT,"
            + COLUMN_RESULT + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_USER_ID_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
            + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_GAME_STATE);
        db.execSQL(CREATE_TABLE_GAME_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);
        onCreate(db);
    }

    public void dropAllTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_STATE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_HISTORY);
    }
}
