package org.opendaycare.repositoryservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("DayCareGroupKid")
//TODO Too many problems with r2dbc and composite PK. Replace this with RAW sql OR create an auto-generated id in addition to the two FK.
public class DayCareGroupKid implements Persistable<Long>{

	
	@Column("idDayCareGroup")
	private Long idDayCareGroup;

	@Column("idKid")
	@Id//TODO Workaround since r2dbc doesn't have support for composite key yet. However, that will prevent one kid from being in multiple groups. Eventually replace repository with raw sql for this.
	private Long idKid;
	
	@Override
	public Long getId() {
		return idKid + idDayCareGroup;//TODO Workaround for the fact that r2dbc only works with null id to create new entities. 
	}

	@Override
	public boolean isNew() {
		return true;//TODO Workaround for the fact that r2dbc only works with null id to create new entities. 
	}
}
