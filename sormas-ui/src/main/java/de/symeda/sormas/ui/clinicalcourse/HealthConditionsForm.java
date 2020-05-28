package de.symeda.sormas.ui.clinicalcourse;

import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.ASPLENIA;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.ASTHMA;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CARDIOVASCULAR_DISEASE_INCLUDING_HYPERTENSION;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CHRONIC_HEART_FAILURE;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CHRONIC_KIDNEY_DISEASE;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CHRONIC_LIVER_DISEASE;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CHRONIC_NEUROLOGIC_CONDITION;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CHRONIC_PULMONARY_DISEASE;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CONGENITAL_SYPHILIS;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.CURRENT_SMOKER;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.DIABETES;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.DOWN_SYNDROME;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.FORMER_SMOKER;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.HEPATITIS;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.HIV;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.HIV_ART;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.I18N_PREFIX;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.IMMUNODEFICIENCY_INCLUDING_HIV;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.IMMUNODEFICIENCY_OTHER_THAN_HIV;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.MALIGNANCY_CHEMOTHERAPY;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.OBESITY;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.OTHER_CONDITIONS;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.SICKLE_CELL_DISEASE;
import static de.symeda.sormas.api.clinicalcourse.HealthConditionsDto.TUBERCULOSIS;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidColumn;
import static de.symeda.sormas.ui.utils.LayoutUtil.fluidRow;
import static de.symeda.sormas.ui.utils.LayoutUtil.h3;
import static de.symeda.sormas.ui.utils.LayoutUtil.loc;
import static de.symeda.sormas.ui.utils.LayoutUtil.locs;

import java.util.Arrays;

import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TextArea;

import de.symeda.sormas.api.clinicalcourse.HealthConditionsDto;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.ui.utils.AbstractEditForm;
import de.symeda.sormas.ui.utils.FieldHelper;

public class HealthConditionsForm extends AbstractEditForm<HealthConditionsDto> {

	private static final long serialVersionUID = 1L;

	private static final String HTML_LAYOUT = h3(I18nProperties.getString(Strings.headingHealthConditions))
		+ fluidRow(
			fluidColumn(
				6,
				0,
				locs(
					TUBERCULOSIS,
					ASPLENIA,
					HEPATITIS,
					DIABETES,
					IMMUNODEFICIENCY_OTHER_THAN_HIV,
					IMMUNODEFICIENCY_INCLUDING_HIV,
					HIV,
					HIV_ART,
					CONGENITAL_SYPHILIS,
					DOWN_SYNDROME,
					CHRONIC_LIVER_DISEASE,
					MALIGNANCY_CHEMOTHERAPY)),
			fluidColumn(
				6,
				0,
				locs(
					CHRONIC_HEART_FAILURE,
					CHRONIC_PULMONARY_DISEASE,
					CHRONIC_KIDNEY_DISEASE,
					CHRONIC_NEUROLOGIC_CONDITION,
					CARDIOVASCULAR_DISEASE_INCLUDING_HYPERTENSION,
					OBESITY,
					CURRENT_SMOKER,
					FORMER_SMOKER,
					ASTHMA,
					SICKLE_CELL_DISEASE)))
		+ loc(OTHER_CONDITIONS);

	public HealthConditionsForm() {
		super(HealthConditionsDto.class, I18N_PREFIX);
	}

	@Override
	protected void addFields() {
		addFields(
			TUBERCULOSIS,
			ASPLENIA,
			HEPATITIS,
			DIABETES,
			HIV,
			HIV_ART,
			CHRONIC_LIVER_DISEASE,
			MALIGNANCY_CHEMOTHERAPY,
			CHRONIC_HEART_FAILURE,
			CHRONIC_PULMONARY_DISEASE,
			CHRONIC_KIDNEY_DISEASE,
			CHRONIC_NEUROLOGIC_CONDITION,
			DOWN_SYNDROME,
			CONGENITAL_SYPHILIS,
			IMMUNODEFICIENCY_OTHER_THAN_HIV,
			CARDIOVASCULAR_DISEASE_INCLUDING_HYPERTENSION,
			OBESITY,
			CURRENT_SMOKER,
			FORMER_SMOKER,
			ASTHMA,
			SICKLE_CELL_DISEASE,
			IMMUNODEFICIENCY_INCLUDING_HIV);
		addField(OTHER_CONDITIONS, TextArea.class).setRows(3);

		for (Object propertyId : getFieldGroup().getBoundPropertyIds()) {
			Field<?> field = getFieldGroup().getField(propertyId);
			if (isFieldHiddenForCurrentCountry(propertyId)) {
				field.setVisible(false);
			}
		}

		FieldHelper.setVisibleWhen(getFieldGroup(), HIV_ART, HIV, Arrays.asList(YesNoUnknown.YES), true);
	}

	@Override
	protected String createHtmlLayout() {
		return HTML_LAYOUT;
	}

}
