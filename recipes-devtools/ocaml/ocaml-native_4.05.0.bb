require ocaml-4.05.inc
inherit native

# hasgot for dlopen often fails on modern hosts (dynamic-linker path quirks),
# leaving SUPPORTS_SHARED_LIBRARIES=false, no dll*.so stublibs, and no -ldl
# in NATIVECCLIBS. Host bytecode tools then fail on dllbigarray, and host
# ocamlopt.opt (used by findlib for target links) omits -ldl while asmrun
# still references dlopen once SUPPORT_DYNAMIC_LINKING is set.
do_configure_append() {
    if [ -f ${S}/config/s.h ] && ! grep -q SUPPORT_DYNAMIC_LINKING ${S}/config/s.h; then
        echo "#define SUPPORT_DYNAMIC_LINKING" >> ${S}/config/s.h
    fi
    if [ -f ${S}/config/Makefile ]; then
        sed -i 's/^SUPPORTS_SHARED_LIBRARIES=.*/SUPPORTS_SHARED_LIBRARIES=true/' ${S}/config/Makefile
        if grep -q 'shared-libs-not-available' ${S}/config/Makefile; then
            sed -i \
                -e 's|^MKDLL=.*|MKDLL=gcc -shared|' \
                -e 's|^MKMAINDLL=.*|MKMAINDLL=gcc -shared|' \
                -e 's|^SHAREDCCCOMPOPTS=.*|SHAREDCCCOMPOPTS=-fPIC|' \
                ${S}/config/Makefile
        fi
        # Ensure -ldl is linked for caml_dlopen (asmrun) and dynload stubs.
        for var in NATIVECCLIBS BYTECCLIBS DYNLINKOPTS; do
            if grep -q "^${var}=" ${S}/config/Makefile && ! grep -q "^${var}=.*-ldl" ${S}/config/Makefile; then
                sed -i "s|^${var}=\\(.*\\)|${var}=\\1 -ldl|" ${S}/config/Makefile
            fi
        done
    fi
}
