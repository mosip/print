# Print
A reference project to use mosip credential service over websub and print a digital card.

## Get started
The print project is a spring boot service. The project has sample card printed as a PDF. 

Set the following properties to setup the service in your environment.
```
mosip.event.hubURL = //Websub url
mosip.partner.id = //your partner id from partner portal
mosip.event.callBackUrl = //call back url for websub so upon a credential issued event the websub will call this url. eg: https://dev.mosip.net/v1/print/print/callback/notifyPrint
```

Once done you are set to go.

## Customizing. 
As its a reference project you may need to customize this quite often.

The v1/print/callback is the api that consumes the credential json. So in case you need to change

## Template
The template used for printing is present in the master data. You can alter that for any look and feel change. Key name in master data for template.
```
RPR_UIN_CARD_TEMPLATE
```
To understand the PDF implementation better you can look at the PrintServiceImpl.java file. 
