package com.google.enterprise.connector.dctm.dctmmockwrap;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import com.google.enterprise.connector.dctm.dfcwrap.ICollection;
import com.google.enterprise.connector.dctm.dfcwrap.IQuery;
import com.google.enterprise.connector.dctm.dfcwrap.ISessionManager;
import com.google.enterprise.connector.mock.MockRepositoryDocumentStore;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryManager;
import com.google.enterprise.connector.mock.jcr.MockJcrQueryResult;
import com.google.enterprise.connector.spi.RepositoryException;

public class MockDmQuery implements IQuery {
	private String query;

	private static final String XPATH_QUERY_STRING_UNBOUNDED_DEFAULT = "//*[@jcr:primaryType='nt:resource'] order by @jcr:lastModified, @jcr:uuid";

	private static final String XPATH_QUERY_STRING_BOUNDED_DEFAULT = "//*[@jcr:primaryType = 'nt:resource' and @jcr:lastModified >= ''{0}''] order by @jcr:lastModified, @jcr:uuid";

	public MockDmQuery() {
		query = "";
	}

	public ICollection execute(ISessionManager sessionManager, int queryType)
			throws RepositoryException {
		if (query.equals("")) {
			return null;
		} else if (query.startsWith(XPATH_QUERY_STRING_UNBOUNDED_DEFAULT
				.substring(0, 15))) {
			try {
				MockRepositoryDocumentStore a = null;
				a = ((MockDmSession) sessionManager.getSession(sessionManager
						.getDocbaseName())).getStore();
				MockJcrQueryManager mrQueryMger = new MockJcrQueryManager(a);

				Query q = mrQueryMger.createQuery(this.query, "xpath");
				QueryResult qr = q.execute();

				MockDmCollection co = new MockDmCollection(qr);
				return co;
			} catch (javax.jcr.RepositoryException e) {
				throw new RepositoryException(e);
			}
		} else {// Authorize query...
			String[] ids = this.query.split("', '");
			ids[0] = ids[0].substring(ids[0].lastIndexOf("'") + 1, ids[0]
					.length());
			ids[ids.length - 1] = ids[ids.length - 1].substring(0,
					ids[ids.length - 1].length() - 2);
			List filteredResults = new MockMockList(ids, sessionManager);

			if (filteredResults != null) {
				QueryResult filteredQR = new MockJcrQueryResult(filteredResults);
				MockDmCollection finalCollection = new MockDmCollection(
						filteredQR);
				return finalCollection;
			} else {
				return null;// null value is tested in DctmAuthorizationManager
				// and won't lead to any NullPointerException
			}
		}
	}

	public void setDQL(String dqlStatement) {

		String goodQuery = "";
		if (dqlStatement.indexOf("select i_chronicle_id from ") == -1) {
			if (dqlStatement.indexOf(" and r_modify_date >= ") != -1) {
				goodQuery = makeBoundedQuery(dqlStatement);
			} else {
				goodQuery = XPATH_QUERY_STRING_UNBOUNDED_DEFAULT;
			}
			this.query = goodQuery;
		} else {
			this.query = dqlStatement;// Authorize query. Will be parsed later
		}
	}

	private String makeBoundedQuery(String dqlStatement) {

		int bound1 = dqlStatement.indexOf(" and r_modify_date >= '")
				+ " and r_modify_date >= '".length();
		int bound2 = dqlStatement.indexOf("' and i_chronicle_id > '");
		int bound3 = bound2 + "' and i_chronicle_id > '".length();

		String date = dqlStatement.substring(bound1, bound2);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss.S");
		String formattedDate = "";
		try {
			Date d1 = df.parse(date);

			MockDmTime dateTime = new MockDmTime(d1);
			formattedDate = dateTime.getFormattedDate();

		} catch (ParseException e) {

		}

		String id = dqlStatement.substring(bound3, dqlStatement
				.lastIndexOf("'"));
		return MessageFormat.format(XPATH_QUERY_STRING_BOUNDED_DEFAULT,
				new Object[] { formattedDate, id });
	}

}
