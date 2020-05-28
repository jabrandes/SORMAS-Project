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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitEvent;
import com.vaadin.v7.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.v7.data.util.converter.Converter.ConversionException;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.OptionGroup;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.EntityDto;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.utils.Diseases;
import de.symeda.sormas.api.utils.HideForCountries;
import de.symeda.sormas.api.utils.HideForCountriesExcept;
import de.symeda.sormas.api.utils.Outbreaks;

public abstract class AbstractEditForm<DTO extends EntityDto> extends AbstractForm<DTO> implements FieldGroup.CommitHandler {// implements DtoEditForm<DTO> {

	private static final long serialVersionUID = 1L;

	private boolean hideValidationUntilNextCommit = false;
	private List<Field<?>> visibleAllowedFields = new ArrayList<>();

	public AbstractEditForm(Class<DTO> type, String propertyI18nPrefix) {
		super(type, propertyI18nPrefix);

		postConstruct();
	}

	protected AbstractEditForm(Class<DTO> type, String propertyI18nPrefix, boolean addFields) {
		super(type, propertyI18nPrefix, addFields);

		postConstruct();
	}

	private void postConstruct() {
		getFieldGroup().addCommitHandler(this);
		setWidth(900, Unit.PIXELS);
	}

	@SuppressWarnings("rawtypes")
	public static CommitDiscardWrapperComponent<? extends AbstractEditForm> buildCommitDiscardWrapper(AbstractEditForm wrappedForm) {
		return new CommitDiscardWrapperComponent<>(wrappedForm, wrappedForm.getFieldGroup());
	}

	@SuppressWarnings("rawtypes")
	public static CommitDiscardWrapperComponent<VerticalLayout> buildCommitDiscardWrapper(AbstractEditForm... wrappedForms) {
		VerticalLayout formsLayout = new VerticalLayout();
		if (wrappedForms.length > 0) { // not perfect, but necessary to make this work in grid views like CaseDataView
			formsLayout.setWidth(wrappedForms[0].getWidth(), wrappedForms[0].getWidthUnits());
		}
		FieldGroup[] fieldGroups = new FieldGroup[wrappedForms.length];
		for (int i = 0; i < wrappedForms.length; i++) {
			formsLayout.addComponent(wrappedForms[i]);
			wrappedForms[i].setWidth(100, Unit.PERCENTAGE);
			fieldGroups[i] = wrappedForms[i].getFieldGroup();
		}
		return new CommitDiscardWrapperComponent<>(formsLayout, fieldGroups);
	}

	@Override
	public void setValue(DTO newFieldValue) throws com.vaadin.v7.data.Property.ReadOnlyException, ConversionException {
		super.setValue(newFieldValue);
	}

	@Override
	public boolean isModified() {
		if (getFieldGroup().isModified()) {
			return true;
		}
		return super.isModified();
	}

	@Override
	public void preCommit(CommitEvent commitEvent) throws CommitException {
		List<Field<?>> customFields = getCustomFields();

		if (hideValidationUntilNextCommit) {
			hideValidationUntilNextCommit = false;
			for (Field<?> field : getFieldGroup().getFields()) {
				if (field instanceof AbstractField) {
					AbstractField<?> abstractField = (AbstractField<?>) field;
					abstractField.setValidationVisible(true);
				}
			}

			for (Field<?> field : customFields) {
				if (field instanceof AbstractField) {
					AbstractField<?> abstractField = (AbstractField<?>) field;
					abstractField.setValidationVisible(true);
				}
			}
		}

		for (Field<?> field : customFields) {
			field.validate();
		}
	}

	/**
	 * Attention (!!!!)
	 * This method is not called when used with CommitDiscardWrapperComponent (uses FieldGroup instead)
	 */
	@Override
	public void commit() throws SourceException, InvalidValueException {

		try {
			getFieldGroup().commit();
		} catch (CommitException e) {
			if (e.getInvalidFields().size() > 0) {
				throw new InvalidValueException(
					e.getInvalidFields().keySet().stream().map(f -> f.getCaption()).collect(Collectors.joining(", ")),
					e.getInvalidFields().values().stream().toArray(InvalidValueException[]::new));
			} else {
				throw new SourceException(this, e);
			}
		}
		super.commit();
	}

	@Override
	public void postCommit(CommitEvent commitEvent) throws CommitException {

	}

	@Override
	public void discard() throws SourceException {
		getFieldGroup().discard();
		super.discard();
	}

	/**
	 * Adds the field to the form by using addField(fieldId, fieldType), but additionally sets up a ValueChangeListener
	 * that makes sure the value that is about to be selected is added to the list of allowed values. This is intended
	 * to be used for Disease fields that might contain a disease that is no longer active in the system and thus will
	 * not be returned by DiseaseHelper.isActivePrimaryDisease(disease).
	 */
	protected ComboBox addDiseaseField(String fieldId, boolean showNonPrimaryDiseases) {
		ComboBox field = addField(fieldId, ComboBox.class);
		if (showNonPrimaryDiseases) {
			addNonPrimaryDiseasesTo(field);
		}
		// Make sure that the ComboBox still contains a pre-selected inactive disease
		field.addValueChangeListener(e -> {
			Object value = e.getProperty().getValue();
			if (value != null && !field.containsId(value)) {
				Item newItem = field.addItem(value);
				newItem.getItemProperty(SormasFieldGroupFieldFactory.CAPTION_PROPERTY_ID).setValue(value.toString());
			}
		});
		return field;
	}

	protected ComboBox addInfrastructureField(String fieldId) {
		ComboBox field = addField(fieldId, ComboBox.class);
		// Make sure that the ComboBox still contains a pre-selected inactive infrastructure entity
		field.addValueChangeListener(e -> {
			Object value = e.getProperty().getValue();
			if (value != null && !field.containsId(value)) {
				field.addItem(value);
			}
		});
		return field;
	}

	@SuppressWarnings({
		"unchecked",
		"rawtypes" })
	@Override
	protected <T extends Field> T addField(String propertyId) {
		return (T) addField(propertyId, Field.class);
	}

	/**
	 * @param allowedDaysInFuture
	 *            How many days in the future the value of this field can be or
	 *            -1 for no restriction at all
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected <T extends Field> T addDateField(String propertyId, Class<T> fieldType, int allowedDaysInFuture) {
		T field = getFieldGroup().buildAndBind(propertyId, (Object) propertyId, fieldType);
		formatField(field, propertyId);
		field.setId(propertyId);
		getContent().addComponent(field, propertyId);
		addFutureDateValidator(field, allowedDaysInFuture);
		return field;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected <T extends Field> void formatField(T field, String propertyId) {

		super.formatField(field, propertyId);

		String caption = I18nProperties.getPrefixCaption(propertyI18nPrefix, propertyId, field.getCaption());
		field.setCaption(caption);

		if (field instanceof AbstractField) {
			AbstractField<?> abstractField = (AbstractField) field;
			abstractField.setDescription(I18nProperties.getPrefixDescription(propertyI18nPrefix, propertyId, abstractField.getDescription()));

			if (hideValidationUntilNextCommit && !abstractField.isInvalidCommitted()) {
				abstractField.setValidationVisible(false);
			}
		}

		String validationError = I18nProperties.getPrefixValidationError(propertyI18nPrefix, propertyId, caption);
		field.setRequiredError(validationError);

		field.setWidth(100, Unit.PERCENTAGE);
	}

	protected void styleAsOptionGroupHorizontal(List<String> fields) {
		for (String field : fields) {
			CssStyles.style((OptionGroup) getFieldGroup().getField(field), ValoTheme.OPTIONGROUP_HORIZONTAL);
		}
	}

	protected void setReadOnly(boolean readOnly, String... fieldOrPropertyIds) {
		for (String propertyId : fieldOrPropertyIds) {
			getField(propertyId).setReadOnly(readOnly);
		}
	}

	@Override
	public void clear() {
		// clear the fields instead of the form itself
		for (Field<?> field : getFieldGroup().getFields()) {
			field.clear();
		}
	}

	protected void setVisible(boolean visible, String... fieldOrPropertyIds) {
		for (String propertyId : fieldOrPropertyIds) {
			if (!visible || isVisibleAllowed(propertyId)) {
				getField(propertyId).setVisible(visible);
			}
		}
	}

	protected void setVisibleClear(boolean visible, String... fieldOrPropertyIds) {
		for (String propertyId : fieldOrPropertyIds) {
			if (!visible || isVisibleAllowed(propertyId)) {
				Field<?> field = getField(propertyId);
				if (!visible) {
					field.clear();
				}
				field.setVisible(visible);
			}
		}
	}

	protected void discard(String... propertyIds) {
		for (String propertyId : propertyIds) {
			getField(propertyId).discard();
		}
	}

	protected void setRequired(boolean required, String... fieldOrPropertyIds) {
		for (String propertyId : fieldOrPropertyIds) {
			Field<?> field = getField(propertyId);
			field.setRequired(required);
		}
	}

	protected void setSoftRequired(boolean required, String... fieldOrPropertyIds) {
		for (String propertyId : fieldOrPropertyIds) {
			Field<?> field = getField(propertyId);
			if (required) {
				FieldHelper.addSoftRequiredStyle(field);
			} else {
				FieldHelper.removeSoftRequiredStyle(field);
			}
		}
	}

	protected void addFieldListeners(String fieldOrPropertyId, ValueChangeListener... listeners) {
		for (ValueChangeListener listener : listeners) {
			getField(fieldOrPropertyId).addValueChangeListener(listener);
		}
	}

	protected void addValidators(String fieldOrPropertyId, Validator... validators) {
		for (Validator validator : validators) {
			getField(fieldOrPropertyId).addValidator(validator);
		}
	}

	protected boolean areFieldsValid(String... propertyIds) {
		return Stream.of(propertyIds).allMatch(p -> getField(p).isValid());
	}

	@Override
	protected String getPropertyI18nPrefix() {
		return propertyI18nPrefix;
	}

	public void hideValidationUntilNextCommit() {

		this.hideValidationUntilNextCommit = true;

		for (Field<?> field : getFieldGroup().getFields()) {
			if (field instanceof AbstractField) {
				AbstractField<?> abstractField = (AbstractField<?>) field;
				if (!abstractField.isInvalidCommitted()) {
					abstractField.setValidationVisible(false);
				}
			}
		}

		for (Field<?> field : getCustomFields()) {
			if (field instanceof AbstractField) {
				AbstractField<?> abstractField = (AbstractField<?>) field;
				if (!abstractField.isInvalidCommitted()) {
					abstractField.setValidationVisible(false);
				}
			}
		}
	}

	protected void addNonPrimaryDiseasesTo(ComboBox diseaseField) {
		List<Disease> diseases = FacadeProvider.getDiseaseConfigurationFacade().getAllDiseases(true, false, true);
		for (Disease disease : diseases) {
			if (diseaseField.getItem(disease) != null) {
				continue;
			}

			Item newItem = diseaseField.addItem(disease);
			newItem.getItemProperty(SormasFieldGroupFieldFactory.CAPTION_PROPERTY_ID).setValue(disease.toString());
		}
	}

	/**
	 * Sets the initial visibilities based on annotations and builds a list of all fields in a form that are allowed to be visible -
	 * this is either because the @Diseases and @Outbreaks annotations are not relevant or at least one of these annotations are present on
	 * the respective field.
	 *
	 * @param disease
	 *            Not null if the @Diseases annotation should be taken into account
	 * @param viewMode
	 *            Not null if the @Outbreaks annotation should be taken into account
	 */
	protected void initializeVisibilitiesAndAllowedVisibilities(Disease disease, ViewMode viewMode) {
		for (Object propertyId : getFieldGroup().getBoundPropertyIds()) {
			Field<?> field = getFieldGroup().getField(propertyId);

			boolean diseaseVisibility;
			if (isFieldHiddenForCurrentCountry(propertyId)) {
				diseaseVisibility = false;
			} else if (disease == null || Diseases.DiseasesConfiguration.isDefinedOrMissing(getType(), (String) propertyId, disease)) {
				diseaseVisibility = true;
			} else {
				diseaseVisibility = false;
			}

			boolean outbreakVisibility = viewMode != ViewMode.SIMPLE || Outbreaks.OutbreaksConfiguration.isDefined(getType(), (String) propertyId);

			if (diseaseVisibility && outbreakVisibility) {
				visibleAllowedFields.add(field);
			} else {
				field.setVisible(false);
			}
		}
	}

	protected boolean isFieldHiddenForCurrentCountry(Object propertyId) {
		try {
			final java.lang.reflect.Field declaredField = getType().getDeclaredField(propertyId.toString());

			final String countryLocale = FacadeProvider.getConfigFacade().getCountryLocale();

			final Predicate<String> currentCountryIsHiddenForField = countryLocale::startsWith;

			if (declaredField.isAnnotationPresent(HideForCountries.class)
				&& Arrays.stream(declaredField.getAnnotation(HideForCountries.class).countries()).anyMatch(currentCountryIsHiddenForField)) {
				return true;
			}
			if (declaredField.isAnnotationPresent(HideForCountriesExcept.class)
				&& Arrays.stream(declaredField.getAnnotation(HideForCountriesExcept.class).countries()).noneMatch(currentCountryIsHiddenForField)) {
				return true;
			}
		} catch (NoSuchFieldException e) {
			// This exception is fine because it should only happen for UUID fields
		}
		return false;
	}

	/**
	 * Returns true if the visibleAllowedFields list is either empty (because all fields are allowed to be visible) or contains
	 * the given field. This needs to be called before EVERY setVisible or setVisibleWhen call.
	 */
	protected boolean isVisibleAllowed(Field<?> field) {
		return visibleAllowedFields.isEmpty() || visibleAllowedFields.contains(field);
	}

	protected boolean isVisibleAllowed(String propertyId) {
		return isVisibleAllowed(getFieldGroup().getField(propertyId));
	}

	protected boolean isGermanServer() {
		return FacadeProvider.getConfigFacade().isGermanServer();
	}

}
