Contributor License Agreement Server
==================

This server is designed for contributors to electronically sign the agreement.
When pull requests come in, the server sets a status depending on whether or
not the Github user has signed.

Building and deploying
--------------------
- To run locally: `mvn app engine:devserver`.  However, you will need to set up an oauth client and redirect, etc to work.
- To deploy: `mvn appengine:update`

Keys
-------------------
In development, you can populate the keys in `development.json`.

In production, you need to set the keys in the database as following:

There are a few keys that need to be set in the database.  They can be set using the [google developer console] (https://console.developers.google.com/). The entity name is "Secrets" and each of the following has a single property named "key".
- *github-webhook-secret* - the secret to validate requests coming in from the webhook
- *github-user-token* - the token for the github user that updates the status of the pull request
- *github-oauth-client-id* - the github oauth client id
- *github-oauth-client-secret* - the github oauth client secret

Rendering the form
--------------------
There is a class called RenderForm.java that can be used to generate the static html for the main site.

License
---------------------

Copyright 2013 Open Whisper Systems

Licensed under the AGPLv3: https://www.gnu.org/licenses/agpl-3.0.html