package org.papdt.miscol.dao;

import org.papdt.miscol.bean.Mistake;
import org.papdt.miscol.utils.Constants.Databases.Files;
import org.papdt.miscol.utils.Constants.Databases.Grades;
import org.papdt.miscol.utils.Constants.Databases.IDbWithIdAndName;
import org.papdt.miscol.utils.Constants.Databases.Subjects;
import org.papdt.miscol.utils.Constants.Databases.QuestionType;
import org.papdt.miscol.utils.Constants.Databases.Tags;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataItemProcessor {

	public static void processMistake(Mistake mistake, SQLiteDatabase db) {
		mistake.setTypeId(convertItemIntoId(mistake.getTypeName(), db,
				QuestionType.TABLE_NAME));
		mistake.setSubjectId(convertItemIntoId(mistake.getSubjectName(), db,
				Subjects.TABLE_NAME));
		mistake.setGradeId(convertItemIntoId(mistake.getGradeName(), db,
				Grades.TABLE_NAME));
		// 以上三项都是必须的
		mistake.setAnswerPhotoId(replaceOrDeletePhotoIdIfNecessary(
				mistake.getAnswerPhotoId(), mistake.getAnswerPhotoPath(), db));
		mistake.setQuestionPhotoId(replaceOrDeletePhotoIdIfNecessary(
				mistake.getQuestionPhotoId(), mistake.getQuestionPhotoPath(),
				db));
	}

	public static int replaceOrDeletePhotoIdIfNecessary(int id, String path,
			SQLiteDatabase db) {
		int result = -1;
		if (id != -1 && path == null) {
			db.execSQL("DELETE FROM " + Files.TABLE_NAME + " WHERE "
					+ Files.KEY_STRING_PATH + "='" + path + "'");
		} else if (id != -1 && path != null) {
			int temp = convertItemIntoId(path, db, Files.TABLE_NAME,
					Files.KEY_STRING_PATH);
			if (temp == id) {
				result = id;
			} else {
				db.execSQL("DELETE FROM " + Files.TABLE_NAME + " WHERE "
						+ Files.KEY_INT_ID + "=" + id);
				result = temp;
			}
		} else if (id == -1 && path != null) {
			result = convertItemIntoId(path, db, Files.TABLE_NAME,
					Files.KEY_STRING_PATH);
		}
		return result;
	}

	public static String convertTagsIntoString(Mistake mistake,
			SQLiteDatabase db) {
		StringBuilder sb = new StringBuilder();
		for (String tag : mistake.getTagNames()) {
			int tagId = convertItemIntoId(tag, db, Tags.TABLE_NAME);
			sb.append(tagId + ",");
		}
		return sb.toString();
	}

	public static int convertItemIntoId(String itemName, SQLiteDatabase db,
			String tableName) {
		return convertItemIntoId(itemName, db, tableName,
				IDbWithIdAndName.KEY_STRING_NAME);
	}

	private static int convertItemIntoId(String itemName, SQLiteDatabase db,
			String tableName, String itemColumnName) {
		int itemId;
		Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE "
				+ itemColumnName + "='" + itemName + "'", null);
		if (cursor.moveToNext()) {
			itemId = cursor.getInt(0);
			cursor.close();
		} else {
			cursor.close();
			db.execSQL("INSERT INTO " + tableName + " (" + itemColumnName
					+ ") VALUES ( '" + itemName + "' )");
			cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE "
					+ itemColumnName + "='" + itemName + "'", null);
			cursor.moveToNext();
			itemId = cursor.getInt(0);
		}
		return itemId;
	}

}
