package org.openmrs.module.shr.cdahandler.api.db.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.db.hibernate.HibernateConceptDAO;
import org.openmrs.module.shr.cdahandler.api.db.CdaImportServiceDAO;

/**
 * Hibernate DAO for CDA import service
 */
public class HibernateCdaImportServiceDAO implements CdaImportServiceDAO {
	
	// Hibernate session factory
	private SessionFactory m_sessionFactory;
	
    /**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
    	this.m_sessionFactory = sessionFactory;
    }

    /**
     * Wrapped DAO
     * @see org.openmrs.module.shr.cdahandler.api.db.CdaImportServiceDAO#saveConceptQuick(org.openmrs.Concept)
     */
	@Override
	public Concept saveConceptQuick(Concept concept) {
		HibernateConceptDAO wrappedDao = new HibernateConceptDAO();
		wrappedDao.setSessionFactory(this.m_sessionFactory);
		return wrappedDao.saveConcept(concept);
	}
	
	@Override
	public List<Order> getOrdersByAccessionNumber(String an, boolean includeVoided) {
		Criteria crit = this.m_sessionFactory.getCurrentSession().createCriteria(Order.class)
				.add(Restrictions.eq("accessionNumber", an));
		if(!includeVoided)
				crit.add(Restrictions.eq("voided", includeVoided));
		return (List<Order>)crit.list();
	}
	
	@Override
	public List<Obs> getObsByAccessionNumber(String an, boolean includeVoided) {
		Criteria crit = this.m_sessionFactory.getCurrentSession().createCriteria(Obs.class)
				.add(Restrictions.eq("accessionNumber", an));
		if(!includeVoided)
				crit.add(Restrictions.eq("voided", includeVoided));
		return (List<Obs>)crit.list();
	}
	
}
