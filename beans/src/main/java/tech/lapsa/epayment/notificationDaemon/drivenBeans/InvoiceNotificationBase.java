package tech.lapsa.epayment.notificationDaemon.drivenBeans;

import static tech.lapsa.epayment.notificationDaemon.drivenBeans.Constants.*;

import java.util.Locale;
import java.util.Properties;

import javax.annotation.Resource;

import tech.lapsa.epayment.domain.Invoice;
import tech.lapsa.java.commons.function.MyObjects;
import tech.lapsa.lapsa.jmsRPC.service.JmsReceiverServiceDrivenBean;
import tech.lapsa.lapsa.jmsRPC.service.JmsSkipValidation;
import tech.lapsa.lapsa.text.TextFactory;
import tech.lapsa.lapsa.text.TextFactory.TextModelBuilder;
import tech.lapsa.lapsa.text.TextFactory.TextModelBuilder.TextModel;

@JmsSkipValidation
public abstract class InvoiceNotificationBase<T extends Invoice> extends JmsReceiverServiceDrivenBean<T> {

    InvoiceNotificationBase(final Class<T> objectClazz) {
	super(objectClazz);
    }

    protected abstract Locale locale(Invoice invoice);

    @Resource(lookup = JNDI_RESOURCE_CONFIGURATION)
    private Properties configurationProperties;

    @Override
    public void receiving(final T invoice, final Properties properties) {
	MyObjects.requireNonNull(invoice, "invoice");

	invoice.unlazy();
	TextModelBuilder textModelBuilder = TextFactory.newModelBuilder() //
		.withLocale(locale(invoice)) //
		.bind("instanceVerb", configurationProperties.getProperty(PROPERTY_INSTANCE_VERB, "")) //
		.bind("invoice", invoice) //
		.bind("order", invoice) //
	;
	textModelBuilder = updateTextModel(textModelBuilder, invoice, properties);
	final TextModel textModel = textModelBuilder //
		.build();

	sendWithModel(textModel, invoice);
    }

    protected abstract TextModelBuilder updateTextModel(TextModelBuilder textModelBuilder, Invoice invoice,
	    Properties properties);

    protected abstract void sendWithModel(TextModel textModel, T order);
}
