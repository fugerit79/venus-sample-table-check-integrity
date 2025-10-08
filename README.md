# venus-sample-table-check-integrity

This is a sample project configured using [fj-doc-maven-plugin init plugin](https://venusdocs.fugerit.org/guide/#maven-plugin-goal-init).

[![Keep a Changelog v1.1.0 badge](https://img.shields.io/badge/changelog-Keep%20a%20Changelog%20v1.1.0-%23E05735)](CHANGELOG.md)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=fugerit79_venus-sample-table-check-integrity&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=fugerit79_venus-sample-table-check-integrity)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=fugerit79_venus-sample-table-check-integrity&metric=coverage)](https://sonarcloud.io/summary/new_code?id=fugerit79_venus-sample-table-check-integrity)
[![License: MIT](https://img.shields.io/badge/License-MIT-teal.svg)](https://opensource.org/licenses/MIT)
[![code of conduct](https://img.shields.io/badge/conduct-Contributor%20Covenant-purple.svg)](https://github.com/fugerit-org/fj-universe/blob/main/CODE_OF_CONDUCT.md)

This project is part of a series of mini tutorial on [Venus Fugerit Doc](https://github.com/fugerit-org/fj-doc),
here you can find the [other tutorials](https://github.com/fugerit79/venus-sample-index).


## Requirement

* JDK 8+ (*)
* Maven 3.8+

(*) Currently FOP not working on [JDK 25, See bug JDK-8368356](https://bugs.openjdk.org/browse/JDK-8368356).

## Project initialization

This project was created with [Venus Maven plugin](https://venusdocs.fugerit.org/guide/#maven-plugin-goal-init)

```shell
mvn org.fugerit.java:fj-doc-maven-plugin:8.16.9:init \
-DgroupId=org.fugerit.java.demo \
-DartifactId=venus-sample-table-check-integrity \
-Dextensions=base,freemarker,mod-fop,mod-poi,mod-opencsv \
-DaddJacoco=true \
-DprojectVersion=1.0.0
```

## table-check-integrity

First of all, we introduce a 'table integrity error' in our ftl template.

For instance, an extra column : 

`<cell><para>$Unexpected body cell!!</para></cell>`

Here is full table : 

```ftl
<table columns="3" colwidths="30;30;40"  width="100" id="data-table" padding="2">
    <row header="true">
        <cell align="center"><para>Name</para></cell>
        <cell align="center"><para>Surname</para></cell>
        <cell align="center"><para>Title</para></cell>
    </row>
    <#if listPeople??>
        <#list listPeople as current>
            <row>
                <cell><para>${current.name}</para></cell>
                <cell><para>${current.surname}</para></cell>
                <cell><para>${current.title}</para></cell>
                <cell><para>$Unexpected body cell!!</para></cell>
            </row>
        </#list>
    </#if>
</table>
```

When rendering to Apache FOP doc type handler, this could lead to exceptions like : 

`org.apache.fop.fo.ValidationException: The column-number or number of cells in the row overflows the number of fo:table-columns specified for the table. (See position 71:8)`

On another type handler (i.e. 'html') this could lead to unexpected layout behaviors.

Using the property  :

`<info name="table-check-integrity">${tableCheckIntegrity!'disabled'}</info>`

We can have our software fail early and log some information on the issue.

In our example : 

```txt
[main] WARN org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheck - Table Integrity Check FAILED : 2
[main] WARN org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheck - Row 1 has 4 columns instead of 3
[main] WARN org.fugerit.java.doc.base.feature.tableintegritycheck.TableIntegrityCheck - Row 2 has 4 columns instead of 3
```

Here is a full POC [DocHelperTest](src/test/java/test/org/fugerit/java/demo/venussampletablecheckintegrity/DocHelperTest.java).

table-check-integrity has three possible behaviors : 

* disabled - no check (default)
* warn - check integrity and log result as warning
* fail - as warn but raise an exception

Check [table-check-integrity](https://venusdocs.fugerit.org/guide/#doc-format-entry-point-extra-feature-table-check-integrity) documentation for more details.