package org.opendaycare.repositoryservice.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
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
@Table("Kid")
public class Kid implements IdentifiableModel<Long> {
	

	@Id
	@Column("idKid")
	private Long id;
	
	@Column("firstName")
	private String firstName;
	
	@Column("lastName")
	private String lastName;
	
	@Column("dateOfBirth")
	private LocalDate dateOfBirth;
	
}
