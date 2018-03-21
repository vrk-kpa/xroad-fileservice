# do not repack jars
%define __jar_repack %{nil}
# produce .elX dist tag on both centos and redhat
%define dist %(/usr/lib/rpm/redhat/dist.sh)

Name:               xroad-fileservice
Version:            %{xroad_fileservice_version}
Release:            %{rel}%{?snapshot}%{?dist}
Summary:            X-Road Service Listing
Group:              Applications/Internet
License:            MIT
Requires:           systemd, jre-1.8.0-headless
Requires(post):     systemd
Requires(preun):    systemd
Requires(postun):   systemd

%define src %{_topdir}
%define dst /usr/lib/xroad-fileservice

%description
X-Road service listing

%prep

%build

%install
mkdir -p %{buildroot}%{dst}
mkdir -p %{buildroot}%{_unitdir}
cp -p %{src}/../../build/libs/xroad-fileservice.jar %{buildroot}%{dst}
cp -p %{src}/SOURCES/%{name}.service %{buildroot}%{_unitdir}
mkdir -p %{buildroot}/var/spool/xroad-fileservice/outgoing
mkdir -p %{buildroot}/var/spool/xroad-fileservice/incoming

%clean
rm -rf %{buildroot}

%files
%defattr(0644,root,root,0755)
%{_unitdir}/%{name}.service
%{dst}/%{name}.jar
%attr(770,xroad-fileservice,xroad-fileservice) /var/spool/xroad-fileservice

%pre
if ! id xroad-fileservice > /dev/null 2>&1 ; then
    adduser --system --no-create-home --shell /bin/false xroad-fileservice
fi

%post
%systemd_post %{name}.service

%preun
%systemd_preun %{name}.service

%postun
%systemd_postun_with_restart %{name}.service

%changelog

