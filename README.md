<h1 align="center">
<a href="https://tyrads.com/">TyrAds</a>
</h1>
<h3 align="center">Level up your gaming experience with
  <div style="text-align: center;">
      <img src="https://tyrrewards.com/wp-content/uploads/2023/03/logo4.png" width="300">
  </div>
  </h3>


[Tyrads](https://tyrads.com/) lets you integrate offers in your app in simple and easy to follow steps.

## Tyrads

**Tyrads** is an open source framework that provides a wrapper for presenting and creating offerwall. It interacts with the Tyrads backend letting you easily show offers with only few lines of code!


> **Note:** Apps designed for [Children and Families program](https://play.google.com/about/families/ads-monetization/) should not be using Tyrads SDK, since Tyrads does not collect data from users less than 13 years old


<br/>

# Getting Started


## Prerequisites

- Min Android SDK 21 or higher using Google Play Services



### Installation

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:


~~~ gradle
	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}

~~~


Step 2. Add the dependency

~~~ gradle
	dependencies {
		implementation 'com.github.tyrads-com:tyrads-sdk-android:LATEST_VERSION'// replace LATEST_VERSION with 0.0.1 or the latest stable version
	}
~~~

Integrating the Tyrads offerwall functionality into your application is a straightforward process that involves a few key steps. By following these integration steps, you can seamlessly incorporate the offerwall feature, enhancing user engagement and potentially generating revenue for your application.

### 1. Initialization

This step initializes the Tyrads SDK within your application. You need to provide the API key and API secret obtained from the Tyrads platform. This allows your app to communicate securely with Tyrads' servers.

~~~ dart

  Tyrads.getInstance().init(context, apiKey: "xyz", apiSecret:"abc123");
~~~

### 2. User Login

Upon initializing the SDK, the mandatory step is to log in the user. However, passing a user ID is optional and is only necessary when the publisher operates its own user system. This login process ensures that user interactions with the offerwall are accurately tracked and attributed within the application.

~~~dart
await Tyrads.getInstance().loginUser(userID: "xxx");//userID is optional 
~~~

### 3. Show Offerwall

Once the SDK is initialized and the user is logged in (if applicable), you can display the offerwall to the user. This typically involves calling a function provided by the Tyrads SDK, such as showOffers. The offerwall is where users can engage with various offers, advertisements, or promotions provided by Tyrads, potentially earning rewards or incentives in the process.


~~~ dart
    Tyrads.getInstance().showOffers();
~~~

 

**Android 12**

Apps updating their target API level to 31 (Android 12) or higher will need to declare a Google Play services normal permission in the AndroidManifest.xml file.

Navigate to the `android/app/src/main` directory inside your project's root, locate the AndroidManifest.xml file and add the following line just before the `<application>`.

~~~xml
<uses-permission android:name="com.google.android.gms.permission.AD_ID" />
~~~

You can read more about Google Advertising ID changes [here](https://support.google.com/googleplay/android-developer/answer/6048248).

<br/>
