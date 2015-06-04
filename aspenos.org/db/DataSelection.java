package org.aspenos.db;


public class DataSelection
{
	private String _table, _fields, _ignore, _where;


	public DataSelection(String table, String fields, String ignore, String where)
	{
		_table = table;
		_fields = fields;
		_ignore = ignore;
		_where = where;
	}

	public String getTable()
	{ return _table; }

	public String getFields()
	{ return _fields; }

	public String getIgnore()
	{ return _ignore; }

	public String getWhere()
	{ return _where; }

}
