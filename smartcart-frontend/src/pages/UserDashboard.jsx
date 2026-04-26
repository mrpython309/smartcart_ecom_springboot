import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { User, MapPin, Package, Mail, Phone, Edit2, Save, X, Plus, Trash2 } from 'lucide-react';
import { userAPI } from '../api/services';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from '../components/Shared';
import toast from 'react-hot-toast';

export default function UserDashboard() {
  const { user } = useAuth();
  const [profile, setProfile] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({});
  const [tab, setTab] = useState('profile');

  useEffect(() => {
    Promise.all([userAPI.getProfile(), userAPI.getAddresses()])
      .then(([pRes, aRes]) => {
        setProfile(pRes.data.data);
        setEditForm({ firstName: pRes.data.data.firstName, lastName: pRes.data.data.lastName, phone: pRes.data.data.phone || '' });
        setAddresses(aRes.data.data);
      }).catch(() => toast.error('Failed to load profile'))
      .finally(() => setLoading(false));
  }, []);

  const handleSaveProfile = async () => {
    try {
      const res = await userAPI.updateProfile(editForm);
      setProfile(res.data.data);
      setEditing(false);
      toast.success('Profile updated');
    } catch { toast.error('Failed to update'); }
  };

  const handleDeleteAddress = async (id) => {
    try {
      await userAPI.deleteAddress(id);
      setAddresses(prev => prev.filter(a => a.id !== id));
      toast.success('Address deleted');
    } catch { toast.error('Failed to delete'); }
  };

  if (loading) return <div className="min-h-[60vh] flex items-center justify-center"><LoadingSpinner size="lg" /></div>;

  const tabs = [
    { key: 'profile', label: 'Profile', icon: User },
    { key: 'addresses', label: 'Addresses', icon: MapPin },
  ];

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">My Account</h1>

      {/* Tabs */}
      <div className="flex gap-2 mb-8 border-b border-gray-200">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors -mb-px ${
              tab === t.key ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
            <t.icon className="w-4 h-4" /> {t.label}
          </button>
        ))}
        <Link to="/orders" className="flex items-center gap-2 px-4 py-3 text-sm font-medium text-gray-500 hover:text-gray-700 border-b-2 border-transparent -mb-px">
          <Package className="w-4 h-4" /> Orders
        </Link>
      </div>

      {tab === 'profile' && profile && (
        <div className="card-flat p-6">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-lg font-bold">Personal Information</h2>
            {!editing ? (
              <button onClick={() => setEditing(true)} className="btn-secondary !py-2 !px-4 text-sm flex items-center gap-2">
                <Edit2 className="w-4 h-4" /> Edit
              </button>
            ) : (
              <div className="flex gap-2">
                <button onClick={handleSaveProfile} className="btn-primary !py-2 !px-4 text-sm flex items-center gap-2">
                  <Save className="w-4 h-4" /> Save
                </button>
                <button onClick={() => setEditing(false)} className="btn-secondary !py-2 !px-4 text-sm"><X className="w-4 h-4" /></button>
              </div>
            )}
          </div>

          {editing ? (
            <div className="grid sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">First Name</label>
                <input className="input-field" value={editForm.firstName} onChange={e => setEditForm({...editForm, firstName: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Last Name</label>
                <input className="input-field" value={editForm.lastName} onChange={e => setEditForm({...editForm, lastName: e.target.value})} />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                <input className="input-field" value={editForm.phone} onChange={e => setEditForm({...editForm, phone: e.target.value})} />
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="flex items-center gap-4">
                <div className="w-16 h-16 rounded-full gradient-primary flex items-center justify-center text-white text-xl font-bold">
                  {profile.firstName?.charAt(0)}{profile.lastName?.charAt(0)}
                </div>
                <div>
                  <p className="text-lg font-semibold">{profile.firstName} {profile.lastName}</p>
                  <p className="text-sm text-gray-500">{profile.role}</p>
                </div>
              </div>
              <div className="grid sm:grid-cols-2 gap-4 mt-4">
                <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                  <Mail className="w-5 h-5 text-gray-400" />
                  <div>
                    <p className="text-xs text-gray-500">Email</p>
                    <p className="text-sm font-medium">{profile.email}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 p-3 bg-gray-50 rounded-xl">
                  <Phone className="w-5 h-5 text-gray-400" />
                  <div>
                    <p className="text-xs text-gray-500">Phone</p>
                    <p className="text-sm font-medium">{profile.phone || 'Not provided'}</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {tab === 'addresses' && (
        <div className="space-y-4">
          {addresses.map(addr => (
            <div key={addr.id} className="card-flat p-4 flex items-start justify-between">
              <div className="flex items-start gap-3">
                <MapPin className="w-5 h-5 text-primary-600 mt-0.5" />
                <div>
                  <p className="text-sm font-medium">{addr.street}</p>
                  <p className="text-sm text-gray-500">{addr.city}, {addr.state} {addr.zipCode}</p>
                  <p className="text-sm text-gray-500">{addr.country}</p>
                  {addr.isDefault && <span className="badge-info mt-2">Default</span>}
                </div>
              </div>
              <button onClick={() => handleDeleteAddress(addr.id)} className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          ))}
          {addresses.length === 0 && <p className="text-sm text-gray-500 text-center py-8">No addresses yet. Add one from checkout.</p>}
        </div>
      )}
    </div>
  );
}
