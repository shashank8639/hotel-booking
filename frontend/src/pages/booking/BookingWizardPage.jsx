import { useEffect } from 'react';
import { Link as RouterLink, useNavigate, useSearchParams } from 'react-router-dom';
import { Alert, Button, Container } from '@mui/material';
import { BookingWizardProvider } from '../../context/BookingWizardContext';
import { useBookingWizard } from '../../hooks/useBookingWizard';
import { useRoomDetails } from '../../hooks/useRoomDetails';
import { useAuth } from '../../hooks/useAuth';
import BookingLayout from '../../layouts/BookingLayout';
import BookingFormStep from '../../components/booking/BookingFormStep';
import GuestFormStep from '../../components/booking/GuestFormStep';
import BookingSummaryStep from '../../components/booking/BookingSummaryStep';
import BookingLoadingScreen from '../../components/booking/BookingLoadingScreen';
import { DetailSkeleton } from '../../components/common/LoadingSkeletons';
import { clearWizardDraft } from '../../utils/wizardDraftStorage';

/**
 * Multi-step booking: Room details → Guest → Summary → (navigate) Payment.
 * Draft persists in sessionStorage until booking is created.
 */
function BookingWizardInner() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const {
    step,
    roomId,
    roomIds,
    hydrateFromSearch,
    setRoom,
    next,
    back,
    setStep,
  } = useBookingWizard();

  const queryRoomId = searchParams.get('roomId');
  const { room, loading } = useRoomDetails(queryRoomId || roomId);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', {
        replace: true,
        state: { from: `/book?${searchParams.toString()}` },
      });
    }
  }, [isAuthenticated, navigate, searchParams]);

  useEffect(() => {
    hydrateFromSearch({
      roomId: queryRoomId ? Number(queryRoomId) : null,
      checkIn: searchParams.get('checkIn') || '',
      checkOut: searchParams.get('checkOut') || '',
      guests: Number(searchParams.get('guests') || 2),
    });
  }, [queryRoomId, searchParams, hydrateFromSearch]);

  useEffect(() => {
    if (room) setRoom(room);
  }, [room, setRoom]);

  if (!queryRoomId && !roomId && !(roomIds && roomIds.length)) {
    return (
      <Container sx={{ py: 4 }}>
        <Alert severity="warning">No room selected.</Alert>
        <Button component={RouterLink} to="/rooms" sx={{ mt: 2 }}>
          Search rooms
        </Button>
      </Container>
    );
  }

  if ((queryRoomId || roomId) && (loading || !room)) {
    return (
      <Container sx={{ py: 4 }}>
        <DetailSkeleton />
      </Container>
    );
  }

  const handleCreated = (booking) => {
    clearWizardDraft();
    setStep(3);
    navigate(`/book/payment/${booking.id}`, { replace: true, state: { booking } });
  };

  return (
    <BookingLayout activeStep={step}>
      {step === 0 && <BookingFormStep onNext={next} />}
      {step === 1 && <GuestFormStep onNext={next} onBack={back} />}
      {step === 2 && (
        <BookingSummaryStep
          onBack={back}
          onCreated={handleCreated}
          onEditGuest={() => setStep(1)}
        />
      )}
      {step >= 3 && (
        <BookingLoadingScreen message="Opening payment…" showLinear />
      )}
    </BookingLayout>
  );
}

export default function BookingWizardPage() {
  return (
    <BookingWizardProvider>
      <BookingWizardInner />
    </BookingWizardProvider>
  );
}
