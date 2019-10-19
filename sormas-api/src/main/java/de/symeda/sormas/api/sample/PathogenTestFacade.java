/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.api.sample;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.utils.ValidationRuntimeException;

@Remote
public interface PathogenTestFacade {

	List<PathogenTestDto> getAllActivePathogenTestsAfter(Date date, String userUuid);
	
	List<PathogenTestDto> getAllBySample(SampleReferenceDto sampleRef);
	
	PathogenTestDto getByUuid(String uuid);
	
	PathogenTestDto savePathogenTest(PathogenTestDto dto);

	List<String> getAllActiveUuids(String userUuid);

	List<PathogenTestDto> getByUuids(List<String> uuids);
	
	List<DashboardTestResultDto> getNewTestResultsForDashboard(RegionReferenceDto regionRef, DistrictReferenceDto districtRef, Disease disease, Date from, Date to, String userUuid);
	
	void deletePathogenTest(String pathogenTestUuid, String userUuid);
	
	boolean hasPathogenTest(SampleReferenceDto sample);
	
	void validate(PathogenTestDto pathogenTest) throws ValidationRuntimeException;
	
	List<String> getDeletedUuidsSince(String userUuid, Date since);
	
}
