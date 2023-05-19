# Notes

## Essentials

* Provide more type safety to OSGi framework when used with Scala
* Provide effectful computations using at least `cats.effect.IO` and IORuntime

## Design decisions

* entitites that mirror OSGi types extends `osgi4cats.Wrapper[OsgiEntity]`
* names follow the original names in the OSGi specification (like `getBundle` and
 not `bundle`)
