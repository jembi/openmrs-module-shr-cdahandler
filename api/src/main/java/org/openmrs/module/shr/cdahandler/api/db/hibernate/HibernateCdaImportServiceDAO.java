package org.openmrs.module.shr.cdahandler.api.db.hibernate;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jfree.util.Log;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.activelist.ActiveListItem;
import org.openmrs.api.db.hibernate.HibernateConceptDAO;
import org.openmrs.module.shr.cdahandler.api.db.CdaImportServiceDAO;
import org.openmrs.module.shr.cdahandler.obs.ExtendedObs;

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

	@Override
    public ConceptReferenceTerm saveReferenceTermQuick(ConceptReferenceTerm referenceTerm) {
		HibernateConceptDAO wrappedDao = new HibernateConceptDAO();
		wrappedDao.setSessionFactory(this.m_sessionFactory);
		return wrappedDao.saveConceptReferenceTerm(referenceTerm);
    }

	/**
	 * Get an extended obs
	 * @see org.openmrs.module.shr.cdahandler.api.db.CdaImportServiceDAO#getExtendedObs(java.lang.Integer)
	 */
	@Override
    public ExtendedObs getExtendedObs(Integer id) {
		return (ExtendedObs)this.m_sessionFactory.getCurrentSession().get(ExtendedObs.class, id);
    }

	/**
	 * Get active list items by accession number
	 * Or rather, the accession number of the obs
	 */
	@Override
	public <T extends ActiveListItem> List<T> getActiveListItemByAccessionNumber(String accessionNumber, Class<T> clazz)
	{

		List<Obs> obs = this.getObsByAccessionNumber(accessionNumber, false);
		if(obs.size() > 0)
		{
			Criterion startObs = Restrictions.in("startObs", obs),
					stopObs = Restrictions.in("stopObs", obs);
			Criteria activeListCrit = this.m_sessionFactory.getCurrentSession().createCriteria(clazz)
					.add(Restrictions.or(startObs, stopObs));
			return (List<T>)activeListCrit.list();
		}
		return new ArrayList<T>();
	}

	/**
	 * Get active list item by obs
	 * @see org.openmrs.module.shr.cdahandler.api.db.CdaImportServiceDAO#getActiveListItemByObs(org.openmrs.Obs, java.lang.Class)
	 */
	@Override
    public <T extends ActiveListItem> List<T> getActiveListItemByObs(Obs obs, Class<T> clazz) {
		Criterion startObs = Restrictions.eq("startObs", obs),
				stopObs = Restrictions.eq("stopObs", obs);
		Criteria activeListCrit = this.m_sessionFactory.getCurrentSession().createCriteria(clazz)
				.add(Restrictions.or(startObs, stopObs));
		return (List<T>)activeListCrit.list();
    }

	/**
	 * Get concept source by name
	 * @see org.openmrs.module.shr.cdahandler.api.db.CdaImportServiceDAO#getConceptSourceByHl7(java.lang.String)
	 */
	@Override
    public ConceptSource getConceptSourceByHl7(String hl7) {

		Criteria crit = this.m_sessionFactory.getCurrentSession().createCriteria(ConceptSource.class)
				.add(Restrictions.eq("hl7Code", hl7));
		return (ConceptSource)crit.uniqueResult();
	}

}
